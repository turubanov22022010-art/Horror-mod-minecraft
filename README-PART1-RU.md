# Horror Events (Minecraft 1.21.1, NeoForge)

Мод включает: систему тревоги (SavedData), менеджер событий, 33 события, трёх существ на GeckoLib,
клиентские эффекты, команду `/horror` и конфиг.

## Сборка
1. JDK 21. В корне выполните один раз: `gradle wrapper --gradle-version 8.14.3`
   (или скопируйте gradlew/gradle-wrapper.jar из шаблона NeoForge MDK).
2. `./gradlew build` — мод появится в `build/libs/`. `./gradlew runClient` — запуск клиента.
3. Версии в `gradle.properties` (neo_version, geckolib_version) и плагин ModDevGradle в `build.gradle`
   не проверялись сборкой — при ошибке разрешения замените на актуальные.

## Структура
- `config/` HorrorConfig, EventConfig — ModConfigSpec, переключатель на каждое событие
- `data/` HorrorWorldData (SavedData), AnxietyManager (день, тревога, шанс)
- `event/` HorrorEvent, ActiveEvent, EventManager, EventRegistry, EventUtil; события в
  `psychological/`, `stalker/`, `glitch/` (каждое — отдельный вложенный класс)
- `entity/` HorrorMob, StalkerEntity, FollowerEntity, MimicEntity (GeckoLib)
- `client/` ClientEffects, FakeCrashScreen, HorrorClient, render/HorrorRenderer
- `network/` ClientEffectPayload, HorrorNetwork
- `command/` HorrorCommand

## Команды (OP 2)
`/horror list`, `/horror trigger <id>`, `/horror stop`, `/horror anxiety get|set <n>`, `/horror reset`

## Ресурсы
- Звуки в `sounds.json` пока ссылаются на ванильные события (`type: event`). Чтобы использовать свои:
  положите .ogg в `assets/horror_events/sounds/` и замените записи на `{"name": "horror_events:<файл>"}`.
- Модели/анимации/текстуры мобов сгенерированы программно (простые кубы). Замените файлами из Blockbench
  с теми же именами: `geo/entity/<mob>.geo.json`, `animations/entity/<mob>.animation.json`,
  `textures/entity/<mob>.png` (кости: root, body, head, arm_l, arm_r, leg_l, leg_r; анимации: idle, walk, run,
  appear, vanish, copy).

## Статус
Код написан без запуска компилятора против NeoForge/Minecraft (нет сети). Возможны мелкие ошибки API —
пришлите вывод `./gradlew build`, и я исправлю.
