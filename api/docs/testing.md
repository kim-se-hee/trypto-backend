# 테스트 전략

**테스트 컨테이너**
- Testcontainers 싱글톤 패턴을 사용한다. 추상 클래스의 static 블록에서 컨테이너를 한 번만 띄우고, 테스트 클래스가 이를 상속한다
- 테스트 클래스마다 컨테이너를 새로 생성하지 않는다

**인수 테스트**
- Cucumber를 이용하여 사용자 시나리오가 정상 작동하는지 검증한다
- BR 한 개에 대해서 대표 시나리오 한 개를 작성한다. 같은 BR 의 boundary·내부 계산·입력 형식 검증 등 세부적인 테스트는 단위 테스트가 담당한다
- `.feature` 파일 위치는 `src/test/resources/features/<도메인>/`. 첫 줄에 `# language: ko` 를 적고 한국어 Gherkin 키워드(`기능`, `시나리오`, `만일`, `그러면`, `그리고`)를 사용한다
- 시나리오는 spec.md 의 BR 번호와 매핑되도록 본문에 의도를 적는다
- 같은 기능의 시나리오에는 모두 `@<feature>` 태그를 붙인다 (kebab-case, 예: `@place-order`). `/implement` 의 인수 테스트 러너가 이 태그로 새 기능 시나리오만 골라 실행한다
- Step Definition 위치는 `src/test/java/.../acceptance/steps/<도메인>/`. 클래스명은 `{도메인}StepDefinition` 으로 작성한다. 줄임말(`StepDef`) 금지
- Step Definition 애노테이션은 `io.cucumber.java.en` 패키지의 `@Given`, `@When`, `@Then`을 사용한다. 한글 애노테이션(`@먼저`, `@만일`, `@그러면`, `@그리고`) 사용 금지
- API 호출은 모듈의 기존 클라이언트(예: `CommonApiClient`)를 생성자 주입으로 사용한다. 없으면 같은 패턴으로 새 메서드를 확장한다
- 응답 검증은 모듈의 응답 래퍼(예: `ApiResponseDto`) 구조에 맞춰 JsonPath 로 검증한다
- 테스트 간 격리를 위해 Cucumber `@Before`에서 데이터를 정리한다
- 테스트 데이터 정리 시 `deleteAll()` 대신 `deleteAllInBatch()`를 사용한다

**도메인 단위 테스트**
- 비즈니스 로직이 복잡하거나 높은 정확성이 필요하여 빠른 피드백이 필요한 경우에만 작성한다
- 예: 슬리피지 계산, 가스비 부족 판별, 수수료 계산 등 엣지 케이스가 많은 로직
- 엣지 케이스는 가능하면 경계값 테스트를 진행한다
- 시간 관련 로직은 `Clock`을 빈으로 주입받아 처리하고 테스트 시 Mock Clock으로 제어한다
- Given-When-Then 패턴을 따른다
- `@DisplayName`에 한국어 설명을 작성하고 메서드명은 `methodName_condition_result` 패턴을 따른다
- 테스트 데이터 정리 시 `deleteAll()` 대신 `deleteAllInBatch()`를 사용한다. 라이프사이클 콜백 없이 단일 DELETE 쿼리로 빠르게 정리된다

**서비스 계층 테스트**
- 비즈니스 로직이 도메인에 있으므로 서비스는 단순 오케스트레이션만 남는다
- 오케스트레이션의 결함은 인수 테스트로 자연스럽게 검증할 수 있다
- 따라서 서비스 계층 테스트는 생략한다
