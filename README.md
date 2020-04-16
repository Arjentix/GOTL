# GOTL. Game Of Thrones Language Interpreter

Проект по предмету *Системное ПО*, представляющий из себя интепретатор собственного языка программирования.

## Сборка и запуск

```bash
mvn package
java -cp target/gotl-1.0.jar ru.arjentix.gotl.GotlUI examples/good_example.gotl
```

## Компоненты

- [x] Лексер
- [x] Парсер
- [ ] Стек-машина

*Лексер* выполняет проверку на правильность отдельных лексем. В терминалогии формальных грамматик он занимается распознаванием **терминалов**. Результатом работы лексера является список **токенов**.

> Токен – пара ***Тип лексемы*** и ***Лексема***. Например: [VAR, a].

*Парсер* выполняет проверку на правильность последовательности токенов. В терминалогии формальных грамматик он занимается распознаванием **нетерминалов**. Результатом работы парсера является сообщение об ошибке в исходном коде программы или ничего.

*Стек-машина* исполняет переданную ей последовательность токенов. Результатом работы стек-машины является исполненный исходный код программы.

## Прочие файлы

В папке *resources* хранится файл ***GOTL.gram***, описывающий формальную грамматику языка. Используется в качестве подсказки.

В папке *examples* хранятся примеры программ на этом языке. Их можно подать на вход интерпретатору.