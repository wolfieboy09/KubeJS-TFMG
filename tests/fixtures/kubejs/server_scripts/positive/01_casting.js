ServerEvents.recipes(event => {
  event.recipes.tfmg.casting(
    [Fluid.of('minecraft:lava', 250)],
    [{
      id: 'minecraft:diamond_pickaxe',
      count: 1,
      components: { 'minecraft:damage': 1 },
      chance: 0.75
    }],
    80
  ).id('kubejs:tfmgjs_fixture/casting')
})
