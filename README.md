# GOTL. Game Of Thrones Language Interpreter

> Внимание! Кругом говнокод, т.к. проект был учебным

Проект по предмету *Системное ПО*, представляющий из себя интепретатор собственного языка программирования.

## Сборка и запуск

```bash
mvn package
java -cp target/gotl-1.0.jar ru.arjentix.gotl.GotlUI examples/good_example.gotl
```

## Компоненты

- [x] Лексер
- [x] Парсер
- [x] Стек-машина
- [x] Структура данных *Лист*
- [x] Структура данных *Хэш-мап*
- [x] Оптимизация триад с кэшированием
- [x] Исключение лишних операций
- [x] Функции
- [x] Симуляция многопоточности

### Лексер

Выполняет проверку на правильность отдельных лексем. В терминалогии формальных грамматик он занимается распознаванием **терминалов**. Результатом работы лексера является список **токенов**.

> Токен – пара ***Тип лексемы*** и ***Лексема***. Например: [VAR, a].

### Парсер

Выполняет проверку на правильность последовательности токенов. В терминалогии формальных грамматик он занимается распознаванием **нетерминалов**. Результатом работы парсера является сообщение об ошибке в исходном коде программы или ничего.

### Стек-машина

Исполняет переданную ей последовательность токенов. Результатом работы стек-машины является исполненный исходный код программы.

### Структура данных Лист

Своя реализация структуры данных *лист* и возможность использовать ее из разрабатываемого языка.

### Структура данных Хэш-мап

Своя реализация структуры данных *хэш-мап* и возможность использовать ее из разрабатываемого языка

### Оптимизация триад с кэшированием

Оптимизация вычислений константных выражений до стадии интерпретации всего кода. Используется метод триад. Результат оптимизированной программы кэшируется. Для примера см. [triad_example.gotl](examples/triad_example.gotl)

### Исключение лишних операций

Оптимизация излишних вычислений до стадии интерпретации всего кода. Уже вычисленные значения не вычисляются заново. Результат оптимизированной программы кэшируется. Для примера см. [excluding_example.gotl](examples/excluding_example.gotl)

### Функции

В языке есть возможность писать свои функции. Функции также проходят стадии оптимизации, имеют свои собственные переменные и не имеют доступа к глобальным переменным. Для примера см. [function_example.gotl](examples/function_example.gotl)

### Симуляция многопоточности

В языке можно использовать многопоточность. Сам механизм реализован подобно симуляции многопоточность в операционной системе: выполняется небольшое количество операций одного потока, а затем происходит переключение. Для примера см. [thread_example.gotl](examples/thread_example.gotl)

## Прочие файлы

В папке *resources* хранится файл ***GOTL.gram***, описывающий формальную грамматику языка. Используется в качестве подсказки.

В папке *examples* хранятся примеры программ на этом языке. Их можно подать на вход интерпретатору.
