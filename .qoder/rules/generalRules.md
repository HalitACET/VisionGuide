# 🚀 VIBE KODDING RULES v2

**Explain + Commit Driven Clean Architecture for Android MVVM**

Bu doküman yalnızca ne yapılacağını değil,\
her mimari adımda **neden yapıldığını ve commit mesajının nasıl olması
gerektiğini** açıklar.

------------------------------------------------------------------------

## 🧱 1. PROJE YAPISI --- (Setup aşaması)

### 🎯 Amaç:

Projeyi temiz bir katmanlı yapıda (data-domain-presentation) başlatmak.

### 🔧 Neden:

Katmanlar arası bağımlılığı minimize etmek, test edilebilirliği
artırmak, sorumlulukları ayırmak.

### 🔨 Nasıl:

    com.example.app/
     ┣ data/
     ┣ domain/
     ┣ presentation/
     

### 💬 Commit:

    chore(structure): setup clean architecture layers (data, domain, presentation)

------------------------------------------------------------------------

## 🧩 2. DATA KATMANI --- (Gerçek veri kaynakları)

### 🎯 Amaç:

API veya local veri kaynaklarını izole etmek.

### 🔧 Neden:

ViewModel doğrudan Retrofit veya Room'a bağımlı olmamalı.\
Data katmanı bu bağımlılıkları yönetir.

### 🔨 Nasıl:

    data/
     ┣ datasources/
     ┃ ┣ remote/ → Retrofit API servisleri
     ┃ ┗ local/ → Room DAO
     ┣ repositories/ → QuizRepositoryImpl.kt
     ┣ models/ → QuizDto.kt, QuizEntity.kt
     ┣ mappers/ → QuizMapper.kt
     ┗ di/ → NetworkModule.kt, RepositoryModule.kt

### 💬 Commit:

    feat(data): implement Retrofit service, Room DAO, mapper and DI modules for Quiz

------------------------------------------------------------------------

## ⚙️ 3. DOMAIN KATMANI --- (İş kuralları)

### 🎯 Amaç:

Veri katmanını soyutlamak ve "use case"'lerle iş mantığını taşımak.

### 🔧 Neden:

ViewModel sadece iş kuralını çağırmalı, verinin nereden geldiğini
bilmemeli.

### 🔨 Nasıl:

    domain/
     ┣ usecases/
     ┃ ┣ GetQuizListUseCase.kt
     ┃ ┗ CalculateScoreUseCase.kt
     ┣ models/ → Quiz.kt
     ┗ repositories/ → QuizRepository.kt

### 💬 Commit:

    feat(domain): add usecases for fetching quiz list and calculating scores

------------------------------------------------------------------------

## 💡 4. PRESENTATION KATMANI --- (UI ve State Yönetimi)

### 🎯 Amaç:

Ekranları yöneten ViewModel ve UI bileşenlerini izole etmek.

### 🔧 Neden:

UI mantığı domain'den ayrı tutulur, böylece test edilmesi kolaylaşır.

### 🔨 Nasıl:

    presentation/
     ┣ viewmodel/
     ┃ ┗ QuizViewModel.kt
     ┣ ui/
     ┃ ┣ screens/ → QuizScreen.kt
     ┃ ┣ components/ → QuizCard.kt
     ┃ ┗ navigation/ → NavGraph.kt
     ┗ state/
     ┃ ┗ QuizUiState.kt

### 💬 Commit:

    feat(presentation): create QuizViewModel and Compose UI with state management

------------------------------------------------------------------------

## 🧠 5. STATE YÖNETİMİ --- (Reactive yapı)

### 🎯 Amaç:

UI'ı ViewModel'den gelen durumlara göre dinamik güncellemek.

### 🔧 Neden:

Kullanıcı arayüzü her veri değişiminde otomatik olarak yenilenmeli.

### 🔨 Nasıl:

-   ViewModel:

``` kotlin
class QuizViewModel(
    private val getQuizListUseCase: GetQuizListUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState

    fun loadQuizzes() {
        viewModelScope.launch {
            try {
                val quizzes = getQuizListUseCase()
                _uiState.value = QuizUiState.Success(quizzes)
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(e.message)
            }
        }
    }
}
```

-   UI:

``` kotlin
@Composable
fun QuizScreen(viewModel: QuizViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsState()
    when (val ui = state.value) {
        is QuizUiState.Loading -> LoadingView()
        is QuizUiState.Success -> QuizList(ui.data)
        is QuizUiState.Error -> ErrorView(ui.message)
    }
}
```

### 💬 Commit:

    refactor(state): implement reactive UI with StateFlow for Quiz screen

------------------------------------------------------------------------

## 🔩 6. DEPENDENCY INJECTION (DI)

### 🎯 Amaç:

Katmanlar arası bağımlılığı yöneten modüller oluşturmak.

### 🔧 Neden:

Bağımlılıklar Hilt üzerinden tek noktadan sağlansın.

### 🔨 Nasıl:

    data/di/
     ┣ NetworkModule.kt
     ┣ RepositoryModule.kt
     ┗ UseCaseModule.kt

### 💬 Commit:

    chore(di): setup Hilt modules for network, repository and usecase injection under data/di

------------------------------------------------------------------------

## 🧪 7. TEST YAPISI

### 🎯 Amaç:

Katmanlar arası izolasyonu testlerle doğrulamak.

### 🔧 Neden:

Refactor sonrası hata riskini azaltmak.

### 🔨 Nasıl:

    src/test/java/
     ┣ domain/usecases/
     ┃ ┗ GetQuizListUseCaseTest.kt
     ┣ data/repositories/
     ┃ ┗ QuizRepositoryImplTest.kt
     ┗ presentation/viewmodel/
     ┃ ┗ QuizViewModelTest.kt

### 💬 Commit:

    test: add unit tests for domain and presentation layers

------------------------------------------------------------------------

## 🧾 8. COMMIT TARZI --- (Conventional Commit + Açıklama)

  -----------------------------------------------------------------------
  Prefix          Ne zaman?               Açıklama örneği
  --------------- ----------------------- -------------------------------
  `feat:`         Yeni özellik            "Yeni UseCase eklendi, quiz
                  eklendiğinde            skor hesaplaması için"

  `fix:`          Hata düzeltildiğinde    "NullPointer hatası düzeltildi,
                                          boş liste kontrolü eklendi"

  `refactor:`     Kod yapısı              "ViewModel logic
                  iyileştirildiğinde      sadeleştirildi"

  `chore:`        Ayar/dependency         "Gradle sürümü güncellendi"
                  değişikliğinde          

  `docs:`         Dokümantasyon           "Readme'ye mimari akışı
                  değiştiğinde            eklendi"
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 🧭 9. KOD AÇIKLAMALARI (Commit + Neden)

Her commit mesajı en az şu üç unsuru içermeli:

    feat(domain): add GetQuizListUseCase

    → NEDEN: ViewModel iş kuralına doğrudan erişmemeli
    → NASIL: Repository arayüzü ile etkileşime giren UseCase oluşturuldu
    → SONUÇ: Test edilebilirlik ve bağımsızlık artırıldı

------------------------------------------------------------------------

## 💎 10. GELİŞTİRME AKIŞI (Örnek Tam Döngü)

  -------------------------------------------------------------------------------------------------------
  Aşama                 Açıklama                     Commit
  --------------------- ---------------------------- ----------------------------------------------------
  Katman oluştur        Yapı başlatıldı              `chore(structure): setup clean architecture`

  Model eklendi         DTO, Entity, Domain modeller `feat(models): add quiz models`
                        tanımlandı                   

  API ve DAO            Veri kaynakları oluşturuldu  `feat(data): add remote and local datasources`

  Repository            Veri erişimi soyutlandı      `feat(data): implement QuizRepositoryImpl`

  UseCase               İş kuralı tanımlandı         `feat(domain): add GetQuizListUseCase`

  ViewModel             StateFlow eklendi            `feat(presentation): add QuizViewModel with state`

  UI                    Compose ekranları eklendi    `feat(ui): build QuizScreen with state handling`

  Test                  Unit testler yazıldı         `test: add tests for usecase and viewmodel`
  -------------------------------------------------------------------------------------------------------
