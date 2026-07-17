ServerEvents.recipes(event => {
  event.recipes.tfmg.polarizing(
    ['minecraft:iron_ingot'],
    ['minecraft:compass']
  )
    .processingTime(60)
    .id('kubejs:tfmgjs_fixture/polarizing')
})
