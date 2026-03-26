---
description: >
  인수 테스트 Gherkin을 UI 시나리오로 번역하고, 전체 사용자 여정으로 조립한 뒤
  Playwright MCP로 E2E 테스트를 수행한다.
  TRIGGER: 사용자가 E2E 테스트를 요청하거나 /e2e-test를 입력할 때.
---

# 인수 테스트 기반 E2E 테스트

인수 테스트 Gherkin을 UI 시나리오로 번역하고, 사용자 여정으로 조립한 뒤 Playwright MCP로 E2E 테스트를 수행한다.

## 입력

`$ARGUMENTS` = 테스트 대상 feature 파일 경로 (공백 구분으로 여러 개 가능)

```
/e2e-test round.feature order.feature wallet-assets.feature
```

feature 파일 경로에 디렉토리가 생략되면 `src/test/resources/features/` 하위로 간주한다.

---

## Phase 0: Playwright 프로젝트 설정

`e2e/` 디렉토리에 `package.json`과 `playwright.config.ts`가 없으면 프로젝트를 초기화한다.

1. `e2e/` 디렉토리 생성 (없으면)
2. `npm init -y && npm install -D @playwright/test`
3. `npx playwright install chromium`
4. `playwright.config.ts` 생성:

```ts
import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: ".",
  testMatch: "**/*.spec.ts",
  timeout: 30_000,
  expect: { timeout: 5_000 },
  fullyParallel: false,
  retries: 0,
  workers: 1,
  reporter: [["html", { open: "always" }], ["list"]],
  use: {
    baseURL: "http://localhost:5173",
    screenshot: "only-on-failure",
    trace: "on-first-retry",
  },
  projects: [
    {
      name: "chromium",
      use: { browserName: "chromium" },
    },
  ],
});
```

5. `package.json`의 scripts에 추가:

```json
{
  "test": "npx playwright test",
  "test:report": "npx playwright show-report"
}
```

이미 설정이 존재하면 이 Phase를 건너뛴다.

---

## Phase 1: 인수 테스트 → UI 시나리오 번역

`$ARGUMENTS`로 지정된 .feature 파일을 모두 읽고, 각 시나리오를 UI 관점으로 번역한다.

**번역 원칙**
- HTTP 상태코드/에러코드 → 사용자가 실제로 보는 UI 피드백으로 변환
- API 호출 → 버튼 클릭, 폼 입력, 페이지 이동 등 사용자 액션으로 변환
- 비즈니스 규칙은 그대로 유지 (최소 금액, 잔고 부족 등)
- 배경(Given)의 데이터 준비는 UI를 통한 사전 액션으로 변환하거나, 불가능하면 API 직접 호출로 남긴다

**예시**

| API 수준 (before) | UI 수준 (after) |
|-------------------|-----------------|
| `만일 지갑의 BTC 입금 주소를 조회한다` | `만일 입금 페이지에서 BTC를 선택한다` → `그러면 입금 주소가 화면에 표시된다` |
| `그러면 응답 상태코드는 200이다` | `그러면 성공 메시지가 표시된다` 또는 결과 UI 요소 확인 |
| `그리고 에러 코드는 "X"이다` | `그러면 "에러 메시지 텍스트"가 표시된다` |
| `만일 기본 라운드 시작 요청을 보낸다` | `만일 라운드 시작 버튼을 클릭한다` → `그리고 시드머니를 입력한다` → `그리고 시작 버튼을 클릭한다` |


**E2E 재현 불가 시나리오:**
- 배치 작업, 서버 내부 스케줄링 등 UI로 재현할 수 없는 시나리오는 스킵한다

---

## Phase 2: spec.ts 스켈레톤 작성

Phase 1에서 번역된 시나리오들을 **바로 spec.ts 파일로 작성**한다. 별도 마크다운 문서를 만들지 않는다 — spec.ts 자체가 테스트 계획서 역할을 한다.

**파일 분리 전략:**

기능 영역별로 별도 spec.ts 파일을 만든다. 하나의 파일에 모든 시나리오를 넣지 않는다.

- **장애 격리**: serial 모드에서 하나의 describe 블록 안에서 테스트가 실패하면 뒤의 테스트가 skipped 된다. 파일을 분리하면 한 파일의 실패가 다른 파일에 영향을 주지 않는다.
- **선택적 실행**: `npx playwright test order-advanced.spec.ts`로 특정 영역만 빠르게 검증할 수 있다.
- **가독성**: 파일명만 보고 어떤 기능을 테스트하는지 파악할 수 있다.

**분리 기준:**
- 핵심 사용자 여정(라운드 시작 → 거래 → 조회 → 종료)은 하나의 파일로 유지한다
- 이후 추가되는 기능별 시나리오(주문 취소, 입출금, 차트 등)는 기능 영역별로 별도 파일로 분리한다
- 서로 다른 페이지(`/market`, `/wallet`, `/mypage`)를 테스트하면 별도 파일이 자연스럽다
- 하나의 파일 안에서 선행 조건이 필요한 테스트들은 같은 파일에 둔다 (예: 시장가 매수 → 지정가 매도)

**파일명 예시:**
```
e2e/
├── full-journey.spec.ts      # 핵심 사용자 여정 (라운드 → 거래 → 조회)
├── order-advanced.spec.ts     # 주문 취소, 지정가 매도
├── transfer.spec.ts           # 입금 주소, 거래소 간 송금, 입출금 내역
├── candle-chart.spec.ts       # 캔들 차트 주기 전환
├── ranker-portfolio.spec.ts   # 랭커 포트폴리오 열람
└── portfolio-visibility.spec.ts # 포트폴리오 공개/비공개 토글
```

**조립 원칙:**

1. **시간순 흐름을 따른다**: 회원가입/로그인 → 라운드 시작 → 거래 → 조회 → 라운드 종료
2. **선행 조건을 체이닝한다**: 주문 시나리오 실행 전에 라운드 시작이 완료되어야 한다. 별도 API setup 대신 이전 시나리오의 결과를 다음 시나리오의 전제로 사용한다.
3. **미구현 테스트는 `test.skip()`으로 표시한다**: 스켈레톤에 모든 테스트를 등록하되, 아직 구현하지 않은 테스트는 `test.skip()`으로 남겨둔다. 이렇게 하면 컨텍스트가 날아가도 어떤 테스트가 남았는지 spec.ts에서 바로 확인할 수 있다.

**스켈레톤 예시:**

```ts
// ── 1단계: 라운드 시작 ──

test.describe("라운드 시작", () => {
  test("라운드 시작 페이지 로드", async () => {
    // TODO: 구현
  });

  test.skip("시드머니 입력 및 시작", async () => {
    // 아직 미구현 — Phase 3에서 채운다
  });
});
```

---

## Phase 3: Playwright MCP로 테스트 구현

Phase 2에서 만든 spec.ts 스켈레톤의 `test.skip()` 테스트를 하나씩 구현한다.

1. Playwright MCP로 앱에 접속하여 UI 구조를 탐색한다.
2. `test.skip()` 테스트를 위에서부터 순서대로 구현하고, 구현이 끝나면 `test.skip()`을 제거한다.
3. 각 테스트 구현 후 해당 파일만 실행하여 통과를 확인한다: `npx playwright test {파일명}.spec.ts`
4. 실패한 테스트가 있으면 원인을 분석하고 성공할 때까지 개선한다.
5. 한 파일의 모든 `test.skip()`이 제거되면 다음 파일로 넘어간다.
6. 모든 파일이 완성되면 `npx playwright test`로 전체 실행하여 파일 간 간섭이 없는지 확인한다.

---

## Phase 4: 결과 보고

1. **결과 보고**: `npx playwright test`로 전체 실행하여 통과/실패 결과를 단계별로 보고한다.
   - 실패 시: 실패 단계, 스크린샷, 예상 vs 실제 결과, 원인 분석
   - 성공 시: 전체 여정 정상 통과 보고

2. **HTML 리포트**: `npx playwright show-report`로 리포트를 브라우저에서 확인한다.

**테스트 코드 구조:**

각 spec.ts 파일은 동일한 보일러플레이트로 시작한다:

```ts
import { test, expect, type Page } from "@playwright/test";

const BASE_URL = "http://localhost:5173";

let page: Page;

test.beforeAll(async ({ browser }) => {
  page = await browser.newPage();
});

test.afterAll(async () => {
  await page.close();
});

test.describe.configure({ mode: "serial" });

// 최상위 describe 없이 단계별 describe로 그룹핑
test.describe("마켓 탐색", () => {
  test("업비트 마켓 페이지 로드", async () => { ... });
  test("빗썸 거래소 탭 전환", async () => { ... });
});

test.describe("시장가 매수 주문", () => {
  test("시장가 매수 실행", async () => { ... });
  test("체결 내역 확인", async () => { ... });
});
```

**장애 격리 동작:**
- 같은 `describe` 블록 안: 테스트 A 실패 → 테스트 B, C는 skipped (serial 모드 특성)
- 다른 `describe` 블록 간: 같은 파일이라도 독립 실행됨
- 다른 spec.ts 파일 간: 완전히 독립. 한 파일이 전부 실패해도 다른 파일은 정상 실행
- HTML 리포트에 passed, failed, skipped 전부 표시됨

**주의 사항:**
- 최상위 `test.describe`로 감싸지 않는다 — 리포트에 불필요한 접두사가 붙는다
- `serial` 모드 + 공유 `page` 인스턴스로 순서 보장
- `getByText()`는 `{ exact: true }`를 기본 사용하거나 `.first()`를 붙여 strict mode 위반을 방지한다
- viewport 밖 요소는 `scrollIntoViewIfNeeded()` 또는 `page.evaluate()`로 JS 클릭한다
- 모달 내부 버튼(Radix UI 등)은 Playwright 클릭이 안 먹을 수 있다 — `page.evaluate()`로 JS 직접 클릭한다