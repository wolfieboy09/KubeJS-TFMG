ServerEvents.recipes(event => {
  const twoRawIron = Ingredient.of('minecraft:raw_iron').withCount(2)

  event.recipes.tfmg.industrial_blasting(
    [twoRawIron],
    [
      Fluid.of('minecraft:lava', 144),
      Fluid.of('minecraft:water', 250),
      Fluid.of('minecraft:water', 125)
    ],
    200
  )
    .hotAirUsage(25)
    .id('kubejs:tfmgjs_fixture/industrial_blasting')
})
