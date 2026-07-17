ServerEvents.recipes(event => {
  const taggedWater = Fluid.ingredientOf('#c:water').withAmount(1000)

  event.recipes.tfmg.distillation(
    [taggedWater],
    [
      Fluid.of('minecraft:water', 750),
      Fluid.of('minecraft:lava', 250)
    ],
    120
  )
    .heated()
    .id('kubejs:tfmgjs_fixture/distillation')
})
