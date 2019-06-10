# [Game of Life] [![CircleCI](https://circleci.com/gh/yenhsun/Gol.svg?style=svg)](https://circleci.com/gh/yenhsun/Gol)


## Description

Simple demo of Game of Life

## Download
```
git clone git@github.com:yenhsun/Gol.git
```

## Idea
![image](https://github.com/yenhsun/Gol/blob/master/basic_design.jpg)

### MVP-like structure
In this code assignment, I choose MVP-like, such as draft above, as structure with dagger2. 

* View deals with most of Android platform related jobs such as clicking events and updating view state. 
* Presenter handles all of interactions between models/controllers & view. Besides, it also keeps almost all of states of businesss logic.

### Testing
Under this MVP-like structure, testing can be easily because of following reasons:

* Presenter observe all of `intents` from android framework and most of actions in presenter are triggered by `intents`. As a result, it is easy to test presenter by sending `intents` manually.
* Most of updates in View are in it function `View.render(State)` and we only need to pass specific `State` to `View.render(State)` when testing.
* It is easy to extract/set objects in View or Presenter when testing by using DI or service locator (Dagger2 in this case).
* Using `Mock` framework (mockk in this case), we can verify the input & ouput of test cases.

### Others

In case of blocking any UI manipulations, I decided to use `HandlerThread` to send all commands, from presenter, to `GameRunner`, the main GOL calculator and both are running on the distinct background threads.

# [Containing 7s]

## Description

Let g(N) be the count of numbers that contain a 7 when you write out all the numbers from 1 to N


[Game of Life]: https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life

[Containing 7s]: https://github.com/yenhsun/Gol/tree/master/app/src/test/java/yhh/q1
