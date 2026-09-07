ServerEvents.recipes(event => {
  event.recipes.tfmg.vat_machine_recipe(
    [Fluid.of('minecraft:water', 250)],
    [Fluid.of('minecraft:lava', 250)]
  )
    .processingTime(100)
    .machines('tfmg:centrifuge')
    .allowedVatTypes('tfmg:steel')
    .minSize(1)
    .heatLevel(0)
    .pressure(1)
    .id('kubejs:tfmgjs_fixture/vat_pressure')
})
