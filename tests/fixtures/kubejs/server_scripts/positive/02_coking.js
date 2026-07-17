ServerEvents.recipes(event => {
  event.recipes.tfmg.coking(
    ['#minecraft:logs'],
    [
      'minecraft:charcoal',
      Fluid.of('minecraft:water', 250),
      Fluid.of('minecraft:lava', 125)
    ],
    100
  ).id('kubejs:tfmgjs_fixture/coking')
})
