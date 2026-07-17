ServerEvents.recipes(event => {
  event.recipes.tfmg.hot_blast(
    [
      Fluid.of('minecraft:water', 500),
      Fluid.of('minecraft:lava', 500)
    ],
    [
      Fluid.of('minecraft:lava', 750),
      Fluid.of('minecraft:water', 250)
    ],
    160
  ).id('kubejs:tfmgjs_fixture/hot_blast')
})
