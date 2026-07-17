ServerEvents.recipes(event => {
  event.recipes.tfmg.vat_machine_recipe(
    [
      'minecraft:sand',
      'minecraft:gravel',
      Fluid.of('minecraft:water', 250)
    ],
    [
      {
        id: 'minecraft:iron_shovel',
        count: 1,
        components: { 'minecraft:damage': 1 },
        chance: 0.5
      },
      Fluid.of('minecraft:lava', 250)
    ]
  )
    .processingTime(140)
    .heated()
    .machines('tfmg:electrode', 'tfmg:electrode')
    .allowAllVatTypes()
    .minSize(3)
    .heatLevel(1)
    .id('kubejs:tfmgjs_fixture/vat_machine_recipe')
})
