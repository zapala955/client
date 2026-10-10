package ru.white.utils.misc;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
final class Db {
   static Map<String, String> build() {
      HashMap v0 = new HashMap(700);
      v0.put("Цвет", "Color");
      v0.put("Режим", "Mode");
      v0.put("Режим цвета", "Color Mode");
      v0.put("Тема", "Theme");
      v0.put("Свой", "Custom");
      v0.put("Прозрачность", "Opacity");
      v0.put("Размер", "Size");
      v0.put("Сила", "Strength");
      v0.put("Скорость", "Speed");
      v0.put("Ширина", "Width");
      v0.put("Высота", "Height");
      v0.put("Радиус", "Radius");
      v0.put("Масштаб", "Scale");
      v0.put("Качество", "Quality");
      v0.put("Насыщенность", "Saturation");
      v0.put("Звук", "Sound");
      v0.put("Громкость", "Volume");
      v0.put("Анимация", "Animation");
      v0.put("Частицы", "Particles");
      v0.put("Дальность", "Range");
      v0.put("Задержка", "Delay");
      v0.put("Угол", "Angle");
      v0.put("Плавность", "Smoothness");
      v0.put("Включено", "Enabled");
      v0.put("Выключено", "Disabled");
      v0.put(
         "У модуля нету описания",
         "No description available for this module"
      );
      v0.put(
         "Делает руки и предметы стеклянными",
         "Makes hands and held items glassy"
      );
      v0.put("Блюр", "Blur");
      v0.put("Сила размытия", "Blur strength");
      v0.put("Сила оттенка", "Tint strength");
      v0.put("Свечение краёв", "Edge Glow");
      v0.put("Сила свечения", "Glow strength");
      v0.put("Шиммер", "Shimmer");
      v0.put("Ширина шиммера", "Shimmer Width");
      v0.put("Период шиммера", "Shimmer Period");
      v0.put("Искажение", "Distortion");
      v0.put("Сила искажения", "Distortion strength");
      v0.put("Ширина искажения", "Distortion width");
      v0.put("Волна", "Wave");
      v0.put("Сила волны", "Wave Strength");
      v0.put("Радиус волны", "Wave Radius");
      v0.put("Время волны", "Wave Time");
      v0.put("Бой", "Combat");
      v0.put("Движение", "Movement");
      v0.put("Рендер", "Render");
      v0.put("Игрок", "Player");
      v0.put("Другое", "Other");
      v0.put(
         "Стрелки на игроков", "Player arrows"
      );
      v0.put(
         "Подсвечивает блоки в радиусе",
         "Highlights blocks within the selected radius"
      );

      v0.put(
         "Автоматически принимает запросы на телепортацию и в клан",
         "Automatically accepts teleport and clan requests"
      );
      v0.put(
         "Делает наведенный блок стеклянным",
         "Makes the targeted block glassy"
      );
      v0.put(
         "Шейдерный ESP с ореолом свечения для сущностей",
         "Shader ESP with glow halos around entities"
      );
      v0.put(
         "Данный модуль был сделан акарачком поэтому может работать через жопу",
         "Made by Akar, so it may be unstable"
      );
      v0.put(
         "Увеличивает скорость игрока",
         "Increases player speed"
      );
      v0.put(
         "хуй знает как обяснить что оно делает",
         "No idea how to explain what this does"
      );
      v0.put(
         "Автоматический наводится и атакует цель",
         "Automatically aims at and attacks targets"
      );
      v0.put(
         "Увеличивает хит бокс игрока",
         "Increases target hitboxes"
      );
      v0.put(
         "Скрывает ваш никнейм",
         "Hides your nickname"
      );
      v0.put(
         "Создает фейк игрока [Бан на SP / HW]",
         "Spawns a fake player [Ban on SP / HW]"
      );
      v0.put(
         "МОДУЛЬ ПИСАЛ АКАР МОЖЕТ РОБОТАТЬ ЧЕРЕЗ ОЧКО И ЧУДА АЛЛАХА",
         "Made by Akar; may only work by sheer luck"
      );
      v0.put(
         "Автоматические заходит на дуэли SpookyTime",
         "Automatically joins SpookyTime duels"
      );
      v0.put(
         "Свап предметов по клавише.",
         "Swaps items with a keybind."
      );
      v0.put(
         "Позволяет использавать придметы без замедления",
         "Lets you use items without slowdown"
      );
      v0.put(
         "Автоматическая аура под кристалы",
         "Automatic crystal aura"
      );
      v0.put(
         "Не дает Attack Aura ударить бота",
         "Prevents Attack Aura from hitting bots"
      );
      v0.put(
         "Помогает скролить предметы",
         "Helps scroll through items"
      );
      v0.put(
         "Автоматическая прокачка клана через факела",
         "Automatically upgrades the clan using torches"
      );
      v0.put(
         "Автоматически варит зелье [не роботает временно]",
         "Automatically brews potions [temporarily broken]"
      );
      v0.put(
         "Автоматический атакует цель на которую вы навились",
         "Automatically attacks the target you are aiming at"
      );
      v0.put("Убирает задержки", "Removes delays");
      v0.put(
         "Автоматически наводит прицел на врагов с симуляцией движений человека",
         "Automatically aims at enemies with human-like mouse movement"
      );
      v0.put(
         "Легитный фарм стекла: сначала наводится, затем с задержкой строит/ломает центр",
         "Legit glass farm: aims first, then places/breaks the center with a delay"
      );
      v0.put(
         "Убирает удары по друзьям",
         "Prevents hitting friends"
      );
      v0.put(
         "Крафт Божьей ауры, простая закупка и простая продажа по биндам.",
         "God Aura crafting, simple buying and selling with keybinds."
      );
      v0.put(
         "Запрещает серверу свапать предметы",
         "Prevents the server from swapping your items"
      );
      v0.put(
         "Отображает ники, мобов и предметы через стены",
         "Shows names, mobs, and items through walls"
      );
      v0.put("Камера свободного палета", "Freecam");
      v0.put("Китайская шляпа", "China Hat");
      v0.put(
         "Убирает всякую хуйню на твоем ебале.",
         "Removes annoying overlays from your screen."
      );
      v0.put(
         "Кастомный прицел", "Custom crosshair"
      );
      v0.put(
         "Позволяет настроить вид GUI клиента",
         "Lets you customize the client GUI look"
      );
      v0.put(
         "Цветовая гамма мира (руки не затрагивает)",
         "World color grading (does not affect hands)"
      );
      v0.put(
         "Кружочек под тобой при прижке",
         "Circle under you when jumping"
      );
      v0.put(
         "Подсвечивает 3 самых дешёвых лота. Поиск и автопродажа по биндам.",
         "Highlights the 3 cheapest listings. Search and auto-sell with keybinds."
      );
      v0.put(
         "Кубики вокруг игрока - прыгают и крутятся",
         "Cubes around the player that jump and spin"
      );
      v0.put("Настройка рук", "Hand customization");
      v0.put(
         "Визуальный эффект когда мало здоровья",
         "Visual effect when health is low"
      );
      v0.put("Отображения таргета", "Target HUD");
      v0.put(
         "Сетка по миру со свечением",
         "Glowing world grid"
      );
      v0.put(
         "Красивый эффект заде игрока при движении",
         "Nice movement trail behind the player"
      );
      v0.put(
         "Лучи в точке падения жемчуга и стрел",
         "Beams at the landing points SettingVisibility pearls and arrows"
      );
      v0.put(
         "Летающие светящиеся кубы с физикой",
         "Flying glowing cubes with physics"
      );
      v0.put(
         "Призрак модельки при лопании тотема",
         "Ghost model on totem pop"
      );
      v0.put("ХЗ ЧО ПИСАТь", "No description");
      v0.put("Темы", "Themes");
      v0.put("+ Создать", "+ Create");
      v0.put("Удалить", "Delete");
      v0.put("Копировать", "Copy");
      v0.put("Вставить", "Paste");
      v0.put("Цвет визуалов", "Visual color");
      v0.put("Цвет фона", "Background color");
      v0.put("Обводка", "Outline");
      v0.put("Светлый текст", "Light text");
      v0.put("Тёмный текст", "Dark text");
      v0.put("Цвет худа", "HUD color");
      v0.put("Цвет разделителей", "Separator color");
      v0.put(
         "Цвет светлого фона",
         "Light background color"
      );
      v0.put("Эффект тени", "Shadow effect");
      v0.put(
         "Эффект искажения", "Distortion effect"
      );
      v0.put("Отображать", "Show");
      v0.put("Уведомлять о", "Notify about");
      v0.put(
         "Громкость уведомления",
         "Notification volume"
      );
      v0.put("Тип уведомления", "Notification type");
      v0.put("Эффекты", "Effects");
      v0.put("Команда", "Command");
      v0.put("Игрок рядом", "Nearby player");
      v0.put("Дистанция игрока", "Player distance");
      v0.put("Конец PvP", "PvP ended");
      v0.put("Принимать", "Accept");
      v0.put("Принимать ТП от", "Accept TP from");
      v0.put(
         "Игнорировать голых",
         "Ignore unarmored players"
      );
      v0.put("Радиус от прицела", "Crosshair radius");
      v0.put("Свап", "Swap");
      v0.put("Исп. фейерверк", "Use firework");
      v0.put("Ресет сундуков", "Reset chests");
      v0.put("Тип", "Type");
      v0.put("Прыжок", "Jump");
      v0.put("Пузырёк опыта", "XP bottle");
      v0.put("Клавиша поиска", "Search bind");
      v0.put("Клавиша продажи", "Sell bind");
      v0.put("Листать страницы", "Flip pages");
      v0.put("Макс. страниц", "Max pages");
      v0.put("Блоки", "Blocks");
      v0.put("Сущности", "Entities");
      v0.put("Вода [пузырьки]", "Water [bubbles]");
      v0.put("Скрывать друзей", "Hide friends");
      v0.put("Анархию", "Anarchy");
      v0.put("Слот 1", "Slot 1");
      v0.put("Слот 2", "Slot 2");
      v0.put("Слот 3", "Slot 3");
      v0.put("Слот 4", "Slot 4");
      v0.put("Слот 5", "Slot 5");
      v0.put("Слот 6", "Slot 6");
      v0.put("Слот 7", "Slot 7");
      v0.put("Слот 8", "Slot 8");
      v0.put("Слот 9", "Slot 9");
      v0.put(
         "Клавиша добавления в друзья",
         "Add friend bind"
      );
      v0.put(
         "Клавиша кидание Эндер жемчуга",
         "Ender pearl bind"
      );
      v0.put(
         "Клавиша координат", "Coordinates bind"
      );
      v0.put("Клавиша хоруса", "Chorus bind");
      v0.put("Клавиша опыта", "XP bind");
      v0.put("Клавиша дезки", "Disorientation bind");
      v0.put("Клавиша явки", "Obvious Dust bind");
      v0.put("Клавиша снежка", "Snowball bind");
      v0.put("Клавиша пласта", "Plast bind");
      v0.put("Клавиша трапки", "Trap bind");
      v0.put("Клавиша з.ветра", "Wind Charge bind");
      v0.put("Клавиша бож.ауры", "God Aura bind");
      v0.put("Клавиша радейки", "Radeyka bind");
      v0.put("Клавиша бож.воды", "God Water bind");
      v0.put("Клавиша Палладина", "Paladin bind");
      v0.put("Клавиша Ассасина", "Assassin bind");
      v0.put(
         "Клавиша Снотворного",
         "Sleeping Potion bind"
      );
      v0.put("Клавиша Гнева", "Wrath bind");
      v0.put("Клавиша стана", "Stun bind");
      v0.put(
         "Клавиша взрывной трапки",
         "Explosive Trap bind"
      );
      v0.put(
         "Клавиша взрывной штуки",
         "Explosive Item bind"
      );
      v0.put("Клавиша кома снега", "Snow Clump bind");
      v0.put("Авто /EventAttack", "Auto /EventAttack");
      v0.put("Закрывать пустой", "Close when empty");
      v0.put("Свечение", "Glow");
      v0.put("Молнии", "Lightning");
      v0.put("Цвет молнии", "Bolt color");
      v0.put("Ширина молнии", "Bolt width");
      v0.put("Ветви молнии", "Bolt branches");
      v0.put("Бинд", "Bind");
      v0.put("Бинд крафта", "Craft bind");
      v0.put("Бинд продажи", "Sell bind");
      v0.put("Бинд фулл авто", "Full-auto bind");
      v0.put("Бинд незера", "Nether bind");
      v0.put("Бинд алмазов", "Diamond bind");
      v0.put("Что покупать", "Item to buy");
      v0.put("Макс цена 1шт", "Max price per item");
      v0.put("Алмазы 64шт", "Diamonds x64");
      v0.put("Резерв денег", "Money reserve");
      v0.put("Анки закупа", "Buy anchors");
      v0.put("Invest порог", "Invest threshold");
      v0.put("Invest сумма", "Invest amount");
      v0.put("Авто инвиз", "Auto invisibility");
      v0.put("Тип инвиза", "Invisibility type");
      v0.put("Что продавать", "Item to sell");
      v0.put("Цена продажи", "Sell price");
      v0.put("Рандом цены", "Randomize price");
      v0.put("Разброс цены", "Price spread");
      v0.put(
         "Авто пауза игроки", "Auto-pause near players"
      );
      v0.put("Радиус игроков", "Player radius");
      v0.put("Слотов AH", "AH slots");
      v0.put("Кол в хп", "HP amount");
      v0.put("Сила тинта", "Tint strength");
      v0.put("Яркость", "Brightness");
      v0.put("Виньетка", "Vignette");
      v0.put("Сила виньетки", "Vignette strength");
      v0.put(
         "Здоровье с элитрами", "Health with elytra"
      );
      v0.put("Динамит", "Dynamite");
      v0.put("Падение", "Fall");
      v0.put("Эндер-кристалл", "End Crystal");
      v0.put("Здоровье", "Health");
      v0.put(
         "Здоровье на элитре",
         "Health while using elytra"
      );
      v0.put(
         "Дистанция до крист", "Distance to crystal"
      );
      v0.put("Дистанция до тнт", "Distance to TNT");
      v0.put(
         "Не свапать если шар", "Do not swap if orb"
      );
      v0.put(
         "Отображения игроков", "Display players"
      );
      v0.put("Отображения мобов", "Display mobs");
      v0.put(
         "Отображения предметов", "Display items"
      );
      v0.put("Вид бокса", "Box style");
      v0.put("Фон", "Background");
      v0.put("Оттенок", "Tint");
      v0.put("Цвет оттенка", "Tint color");
      v0.put(
         "Скорость перемещения", "Movement speed"
      );
      v0.put("Режим анимации", "Animation mode");
      v0.put("Режим Swing", "Swing mode");
      v0.put(
         "Режим интерполяции", "Interpolation mode"
      );
      v0.put("Только с аурой", "Only with aura");
      v0.put("Угол наклона", "Tilt angle");
      v0.put("Кнопка колеса", "Wheel bind");
      v0.put(
         "Колесо эмоций и кастомных анимаций",
         "Radial emote wheel and custom animations"
      );
      v0.put(
         "Выброшенные предметы лежат на земле",
         "Dropped items lie on the ground"
      );
      v0.put("Cкорость анимации", "Animation speed");
      v0.put("Скорость анимации", "Animation speed");
      v0.put("Сила анимации", "Animation strength");
      v0.put(
         "Анимации взмаха руки",
         "Hand swing animations"
      );
      v0.put(
         "Боксы сущностей через стены",
         "Entity boxes through walls"
      );
      v0.put(
         "Отображает ники, хп и броню сущностей",
         "Shows entity names, HP, and armor"
      );
      v0.put(
         "Положение и размер рук от первого лица",
         "First-person hand position and size"
      );
      v0.put("Правая рука", "Right hand");
      v0.put("Правая X", "Right X");
      v0.put("Правая Y", "Right Y");
      v0.put("Правая Z", "Right Z");
      v0.put("Правая размер", "Right hand size");
      v0.put("Сбросить правую", "Reset right");
      v0.put("Левая рука", "Left hand");
      v0.put("Левая X", "Left X");
      v0.put("Левая Y", "Left Y");
      v0.put("Левая Z", "Left Z");
      v0.put("Левая размер", "Left hand size");
      v0.put("Сбросить левую", "Reset left");
      v0.put("Сбросить всё", "Reset all");
      v0.put("Позиция руки", "Hand position");
      v0.put("Позиция права X", "Right hand position X");
      v0.put("Позиция права Y", "Right hand position Y");
      v0.put("Позиция права Z", "Right hand position Z");
      v0.put("Позиция лево X", "Left hand position X");
      v0.put("Позиция лево Y", "Left hand position Y");
      v0.put("Позиция лево Z", "Left hand position Z");
      v0.put("Звёзды", "Stars");
      v0.put("Цвет 1", "Color 1");
      v0.put("Цвет 2", "Color 2");
      v0.put("Ванильное небо", "Vanilla sky");
      v0.put("Первый предмет", "First item");
      v0.put("Второй предмет", "Second item");
      v0.put("Клавиша свапа", "Swap key");
      v0.put(
         "Уведомления о свапе", "Swap notifications"
      );
      v0.put(
         "Только Зач. Тотем", "Only enchanted totem"
      );
      v0.put(
         "Анимации тройного", "Triple animation"
      );
      v0.put("Убирать огонь", "Remove fire overlay");
      v0.put(
         "Убирать туман лавы", "Remove lava fog"
      );
      v0.put(
         "Убирать плохие эффекты",
         "Remove negative effects"
      );
      v0.put("Убирать Скорборт", "Remove scoreboard");
      v0.put("Убирать Босс бар", "Remove boss bar");
      v0.put(
         "Камера сквозь блоки",
         "Camera through blocks"
      );
      v0.put(
         "Убирать тотем на экране",
         "Remove totem pop animation"
      );
      v0.put(
         "Убирать дерганее камеры",
         "Remove camera shake"
      );
      v0.put("Кол кубиков", "Cube count");
      v0.put("Размер куба", "Cube size");
      v0.put("Локальный игрок", "Local player");
      v0.put("Игроки", "Players");
      v0.put("Мобы", "Mobs");
      v0.put("Предметы", "Items");
      v0.put("Цвет друзей", "Friend color");
      v0.put("Радиус атаки", "Attack range");
      v0.put(
         "Радиус обнаружения", "Detection range"
      );
      v0.put("Кого атаковать", "Targets");
      v0.put("Тип наведения", "Aim type");
      v0.put("Режим снапа", "Snap mode");
      v0.put("Тип спринта", "Sprint mode");
      v0.put(
         "Коррекция движения",
         "Movement correction"
      );
      v0.put("Доп. проверки", "Extra checks");
      v0.put("Не бить если", "Do not hit if");
      v0.put("Тип призраков", "Ghost type");
      v0.put("Тип картинки", "Image type");
      v0.put("Скорость призраков", "Ghost speed");
      v0.put("Размер свечения", "Glow size");
      v0.put("Скорость картинки", "Image speed");
      v0.put("Размер картинки", "Image size");
      v0.put("Скорость кольца", "Ring speed");
      v0.put("Скорость бублика", "Donut speed");
      v0.put("Размер бублика", "Donut size");
      v0.put("Кубиков", "Cubes");
      v0.put("Атаковать игроков", "Attack players");
      v0.put(
         "Атаковать голых (без брони)",
         "Attack unarmored players"
      );
      v0.put("Атаковать мобов", "Attack mobs");
      v0.put("Атаковать друзей", "Attack friends");
      v0.put(
         "Плавность по горизонтали",
         "Horizontal smoothness"
      );
      v0.put(
         "Плавность по вертикали",
         "Vertical smoothness"
      );
      v0.put(
         "Умная остановка (не дергать при наведении)",
         "Smart stop (no jitter when locked on)"
      );
      v0.put(
         "Умная скорость (Реализм)",
         "Smart speed (realism)"
      );
      v0.put(
         "Доводка при ударе (Множитель)",
         "Aim correction on hit (multiplier)"
      );
      v0.put(
         "Случайное изменение скорости",
         "Random speed variation"
      );
      v0.put(
         "Дрожание прицела (Шум)",
         "Aim jitter (noise)"
      );
      v0.put("Сила дрожания", "Jitter strength");
      v0.put("Скорость дрожания", "Jitter speed");
      v0.put(
         "Нейро человечность", "Neuro humanization"
      );
      v0.put("Нейро реакция", "Neuro reaction");
      v0.put(
         "Нейро скорость мыши",
         "Neuro mouse speed"
      );
      v0.put(
         "Атаковать голых", "Attack unarmored players"
      );
      v0.put("Умные криты", "Smart crits");
      v0.put("Бить через блоки", "Hit through blocks");
      v0.put("Не бить при еде", "Do not hit while eating");
      v0.put("Сброс спринта", "Sprint reset");
      v0.put("Размер хит бокса", "Hitbox size");
      v0.put("Режим ротации", "Rotation mode");
      v0.put(
         "Задержка постановки (мс)",
         "Place delay (ms)"
      );
      v0.put(
         "Задержка взрыва (мс)", "Break delay (ms)"
      );
      v0.put("Наведение на блок", "Aim at block");
      v0.put("Только возле врага", "Only near enemy");
      v0.put("Дальность врага", "Enemy range");
      v0.put("Только обсидиан", "Only obsidian");
      v0.put("При атаке", "On attack");
      v0.put("Кол в атаке", "Amount on attack");
      v0.put("При тотеме", "On totem");
      v0.put("Кол при тотеме", "Amount on totem");
      v0.put("При кидании", "On throw");
      v0.put("Кол при кидании", "Amount on throw");
      v0.put("В мире", "In world");
      v0.put("Кол в мире", "Amount in world");
      v0.put("При движении", "On movement");
      v0.put("Тип частиц", "Particle type");
      v0.put("Свечения", "Glow");
      v0.put("Ротация", "Rotation");
      v0.put("Менять время", "Change time");
      v0.put("Менять туман", "Change fog");
      v0.put("Время", "Time");
      v0.put("Дистанция тумана", "Fog distance");
      v0.put("Цвет мира", "World color");
      v0.put("Режим цвета мира", "World color mode");
      v0.put("Цвет освещения", "Lighting color");
      v0.put("Радиус (ячейки)", "Radius (cells)");
      v0.put("Высота (ячейки)", "Height (cells)");
      v0.put("Прозрачность линий", "Line opacity");
      v0.put("Свечение в углах", "Corner glow");
      v0.put("Яркость свечения", "Glow brightness");
      v0.put("Вращение", "Rotation");
      v0.put("Длина шлейфа", "Trail length");
      v0.put("Сглаживание", "Smoothing");
      v0.put("Сегменты", "Segments");
      v0.put("Точки", "Dots");
      v0.put("Высота подъёма", "Rise height");
      v0.put("Длительность", "Duration");
      v0.put("Линия полёта", "Flight path");
      v0.put("Летящие снаряды", "Flying projectiles");
      v0.put("Чьи снаряды", "Projectile owner");
      v0.put("Радиус снарядов", "Projectile radius");
      v0.put("Неймтаг", "Nametag");
      v0.put("Толщина линии", "Line thickness");
      v0.put("Высота луча", "Beam height");
      v0.put("Ширина луча", "Beam width");
      v0.put("Друзей", "Friends");
      v0.put("Всех", "All");
      v0.put("ФанТайм", "FunTime");
      v0.put("Грим", "Grim");
      v0.put("Тики", "Ticks");
      v0.put("Обычный", "Normal");
      v0.put("Простой", "Simple");
      v0.put("Точка", "Dot");
      v0.put("Нейро", "Neuro");
      v0.put("Серый фон", "Gray background");
      v0.put("Размывать фон", "Blur background");
      v0.put("Шейдер", "Shader");
      v0.put("Игроков", "Players");
      v0.put("Голых игроков", "Unarmored players");
      v0.put("Мобов", "Mobs");
      v0.put("Сфокусированная", "Focused");
      v0.put("Свободная", "Free");
      v0.put("Используешь еду", "While eating");
      v0.put("Открыт контейнер", "Container is open");
      v0.put("Запрос на ТП", "TP request");
      v0.put("Запрос в клан", "Clan request");
      v0.put("Двойной", "Double");
      v0.put("Тройной", "Triple");
      v0.put("Шар", "Orb");
      v0.put("Золотое яблоко", "Golden apple");
      v0.put("Щит", "Shield");
      v0.put("Тотем", "Totem");
      v0.put("Шар 2", "Orb 2");
      v0.put("Золотое яблоко 2", "Golden apple 2");
      v0.put("Щит 2", "Shield 2");
      v0.put("Тотем 2", "Totem 2");
      v0.put("Постоянный", "Constant");
      v0.put("При ударе", "On hit");
      v0.put("Отключен", "Off");
      v0.put("Отключено", "Off");
      v0.put("2д", "2D");
      v0.put("3д", "3D");
      v0.put("Обратный", "Reversed");
      v0.put("Снаружи", "Outside");
      v0.put("Оба", "Both");
      v0.put("Внутри", "Inside");
      v0.put("Призраки", "Ghosts");
      v0.put("Картинка", "Image");
      v0.put("Кольцо", "Ring");
      v0.put("Бублик", "Donut");
      v0.put("Кубики", "Cubes");
      v0.put("Клиент", "Client");
      v0.put("Случайный", "Random");
      v0.put("Камера", "Camera");
      v0.put("Свои", "Mine");
      v0.put("Все", "All");
      v0.put("Первый", "First");
      v0.put("Второй", "Second");
      v0.put("Третий", "Third");
      v0.put("Четвёртый", "Fourth");
      v0.put("Пятый", "Fifth");
      v0.put("Шестой", "Sixth");
      v0.put("эффектах", "Effects");
      v0.put("Лого клиента", "Client logo");
      v0.put("Координаты", "Coordinates");
      v0.put("BPS и TPS", "BPS and TPS");
      v0.put("Активные эффекты", "Active effects");
      v0.put("Активные клавиши", "Active keybinds");
      v0.put("Активный таргет", "Active target");
      v0.put("Аррай лист", "Array List");
      v0.put("модулях", "Modules");
      v0.put("броне", "Armor");
      v0.put("Музыка на фоне", "Background music");
      v0.put("Не свап при еде", "No swap while eating");
      v0.put(
         "Принимать запрос в клан только от друзей",
         "Accept clan requests only from friends"
      );
      v0.put(
         "Позволяет крутить свободно камерой.",
         "Lets you freely rotate the camera."
      );
      v0.put("Зажать", "Hold");
      v0.put("Нажать", "Toggle");
      v0.put("Клавиша", "Key");
      v0.put(
         "Во время ПВП блокирует выход с сервера и команды выхода (/hub /limbo /an /anarchy)",
         "Blocks leaving the server and exit commands during PvP (/hub /limbo /an /anarchy)"
      );
      String temp1 = "Убирает сторонние хуки и инжекты";
      int v3 = 519074173;

      v0.put(temp1, StrDec.d("jBTAyx1hUxGA897M5QM6anjRoLD3XzuOU++CBNF6F5FcvGY4qgPmqg==", v3));
      v0.put(
         "Мелкие настройки отображения мира",
         "Minor world rendering tweaks"
      );
      v0.put(
         "Полная яркость (фуллбрайт)",
         "Full brightness (Fullbright)"
      );
      v0.put(
         "Светлячки вокруг игрока",
         "Fireflies around the player"
      );
      v0.put(
         "Кастомные частицы вокруг игрока",
         "Custom particles around the player"
      );
      v0.put(
         "Автоматически держит тотем в руке",
         "Automatically keeps a totem in your hand"
      );
      v0.put(
         "Не даёт другим игрокам и воде толкать вас",
         "Prevents players and water from pushing you"
      );
      v0.put(
         "Блокирует смену слота хотбара",
         "Locks the selected hotbar slot"
      );
      v0.put(
         "Позволяет двигаться с открытым инвентарём",
         "Lets you move while the inventory is open"
      );
      v0.put(
         "Помогает кликать по клавише (автокликер)",
         "Helps click with a keybind (autoclicker)"
      );
      v0.put(
         "Автоматически выбирает лучший инструмент",
         "Automatically selects the best tool"
      );
      v0.put(
         "Помощник элитры: авто-свап и полёт",
         "Elytra helper: auto-swap and flight"
      );
      v0.put(
         "Позволяет лазить по стенам",
         "Lets you climb walls"
      );
      v0.put(
         "Настройка интерфейса (HUD) клиента",
         "Client HUD customization"
      );
      v0.put(
         "Автоматический спринт",
         "Automatic sprint"
      );
      v0.put("Предметы в руках", "Held items");
      v0.put("Отображение таргета", "Target HUD");
      v0.put("Духи", "Spirits");
      v0.put("Затемнять фон", "Dim the background");
      v0.put("Кастомный", "Custom");
      v0.put("Кружочек", "Small circle");
      v0.put("Длина", "Length");
      v0.put("Зазор", "Gap");
      v0.put(
         "Сила анимации при ударе",
         "Animation strength on hit"
      );
      v0.put("Не отображать", "Do not display");
      v0.put("Периодически", "Periodically");
      v0.put("При убийстве", "In the event SettingVisibility a murder");
      v0.put(
         "Объемный туман, дождь и процедурные молнии",
         "Volumetric fog, rain, and procedural lightning"
      );
      v0.put("Туман", "Fog");
      v0.put("Ночная дымка", "Night Haze");
      v0.put("Осадки", "Precipitation");
      v0.put("Тип осадков", "Precipitation Type");
      v0.put("Дождь", "Rain");
      v0.put("Снег", "Snow");
      v0.put("Молнии", "Lightning");
      v0.put("Расширенные", "Advanced");
      v0.put("Цвет тумана", "Fog Color");
      v0.put("Свой цвет тумана", "Custom Fog Color");
      v0.put("Плотность тумана", "Fog Density");
      v0.put("Высота тумана", "Fog Height");
      v0.put("Освещение тумана", "Fog Ambient");
      v0.put(
         "Синхронизация с небом",
         "Sync With Sky"
      );
      v0.put("Сила синхронизации", "Sync Amount");
      v0.put("Усиление заката", "Sunset Boost");
      v0.put("Турбулентность", "Turbulence");
      v0.put("Рассеивание солнца", "Sun Scatter");
      v0.put("Штормовой туман", "Storm Fog");
      v0.put("Цвет дымки", "Haze Color");
      v0.put("Свой цвет дымки", "Custom Haze Color");
      v0.put("Количество дымки", "Haze Amount");
      v0.put("Толщина дымки", "Haze Thickness");
      v0.put("Следовать за землей", "Follow Ground");
      v0.put("Смещение дымки", "Haze Offset");
      v0.put("Уровень дымки", "Haze Level");
      v0.put("Клубы дымки", "Haze Banks");
      v0.put("Свечение дымки", "Haze Glow");
      v0.put("Рассеивание луны", "Moon Scatter");
      v0.put("Только ночью", "Night Only");
      v0.put("Плотность осадков", "Rain Density");
      v0.put("Скорость осадков", "Rain Speed");
      v0.put("Цвет осадков", "Rain Color");
      v0.put("Прозрачность осадков", "Rain Opacity");
      v0.put("Круги на воде", "Ripples");
      v0.put("Сила кругов", "Ripple Strength");
      v0.put("Источник шторма", "Storm Source");
      v0.put("Сила шторма", "Storm Power");
      v0.put("Частота ударов", "Strike Rate");
      v0.put("Дистанция ударов", "Strike Distance");
      v0.put("Цвет молнии", "Bolt Color");
      v0.put("Толщина молнии", "Bolt Width");
      v0.put("Ветви молнии", "Bolt Branches");
      v0.put("Вспышка", "Flash");
      return v0;
   }

   private Db() {
   }

   
}
