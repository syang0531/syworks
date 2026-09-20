# Yame

바닐라 자연블록을 **추출**해 새 금속을 얻고, 이를 **합금**해 청동·강철·티타늄 등 확장 장비를 만드는
Minecraft **26.2** / **NeoForge 26.2** (Java 25) 모드. 형제 모드 `placitum`과 같은 스택이라 함께 플레이할 수 있다.

전체 기획은 [docs/기획.md](docs/기획.md) 참고.

## 요구 사항

- **JDK 25** — 없으면 Gradle(foojay 툴체인)이 `~/.gradle/jdks`에 자동으로 받는다.
- Gradle은 별도 설치 불필요 — 포함된 Gradle Wrapper(`gradlew`)가 자동 처리.

## 빌드 & 실행

PowerShell 기준. 툴체인이 JDK 25를 자동 선택하므로 `JAVA_HOME` 지정은 필요 없다(다른 JDK를 쓰려면 `JAVA_HOME`만 바꾸면 된다).

```powershell
# 1) 리소스(모델/영문 lang) 생성 — 최초 1회 및 아이템 추가 시
.\gradlew.bat runData

# 2) 클라이언트 실행 (개발용 마인크래프트)
.\gradlew.bat runClient

# 컴파일만 확인
.\gradlew.bat compileJava

# 배포용 jar (build/libs/)
.\gradlew.bat build
```

> 최초 실행 시 NeoForge·마인크래프트를 내려받고 디컴파일하므로 몇 분 걸립니다.

## 현재 구현 상태

- ✅ 순수 금속 **11종** 잉곳 (주석·아연·니켈·알루미늄·은·크롬·티타늄·코발트·텅스텐·황·백금)
- ✅ 합금 **12종** 잉곳 + 커스텀 티어 (초경합금·백금 초합금 포함)
- ✅ 합금별 **장비 풀세트** — 도구 5종(검·곡괭이·도끼·삽·괭이) + 방어구 4종(투구·흉갑·레깅스·부츠)
- ✅ **추출기 완성** — GUI(화로 재사용) + 연료/진행도 + 추출 레시피 11종 + 호퍼 자동화
- ✅ **합금로 완성** — 입력 3슬롯 커스텀 GUI + 합금 레시피 12종
- ✅ **엔드게임 티어** — 초경합금(네더라이트 ×2) → 백금 초합금(×2.25, 최종). 둘 다 **화염 저항**. 백금은 고대잔해에서 추출.
- ✅ **장비 제작 레시피** — 잉곳→장비 표준 바닐라 패턴 108종(데이터젠 자동 생성). 서바이벌 전체 루프 완성.
- ✅ 크리에이티브 탭, 한/영 이름, 플레이스홀더 텍스처(아이템·방어구 레이어)
- ✅ 런타임 검증 — 데디케이티드 서버 부팅, `Loaded 1421 recipes` 파싱 오류 0 (모드 레시피 131종 전부)
- ⏳ **다음 후보**: 실제 텍스처 아트 · 인게임 밸런스 튜닝

전체 루프: `광석블록 →(추출기)→ 순수금속 →(합금로)→ 합금잉곳 →(제작대)→ 도구·방어구`

### 사용법 (게임 내)
- **추출기**: 우클릭 → 위=소스 블록(화강암·섬록암·점토 등), 아래=연료(석탄) → 오른쪽에서 금속 잉곳 회수.
- **합금로**: 우클릭 → 왼쪽 3칸=금속(예: 구리×3 + 주석×1), 연료칸=석탄 → 오른쪽에서 합금 잉곳(청동) 회수.

플레이스홀더 텍스처는 재질별 색상만 다른 단순 아이콘입니다(추후 실제 아트로 교체).

## 구조

```
src/main/java/com/syang/yame/
├── Yame.java              메인 (@Mod)
├── registry/                      DeferredRegister (items, blocks, tabs)
├── world/item/                    ModMetal, ModAlloy(밸런스), ModTier
├── world/level/block/             ExtractionFurnaceBlock, AlloyFurnaceBlock
└── datagen/                       모델·영문 lang 생성기
src/main/resources/
├── META-INF/neoforge.mods.toml
└── assets/yame/lang/ko_kr.json
```

밸런스 수치는 [ModAlloy.java](src/main/java/com/syang/yame/world/item/ModAlloy.java) enum에서 한 곳에서 조정.
