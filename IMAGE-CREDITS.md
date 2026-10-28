# Sample image credits

The two bundled images are AI-generated illustrations produced with ChatGPT's built-in image-generation tool on 28 October 2026. They are not photographs supplied by TheMealDB and are not evidence that its Indian endpoint returned recipes.

Files:
- `app/src/main/res/drawable-nodpi/sample_biryani.png`
- `app/src/main/res/drawable-nodpi/sample_chana_masala.png`

They are displayed only for explicit sample-mode IDs `test-1` and `test-2`. The UI labels each "AI-generated sample image". Live recipes continue to use their API `strMealThumb`. Original missing/invalid URLs remain in JSON fixtures for parser/failure tests; at runtime the two known sample IDs use local resources instead. Old saved sample favourites use the same ID mapping, so no reset of saved favourites is required.

## Prompt set (built-in image generation)

### Biryani

Photorealistic editorial food illustration for an offline sample Indian recipe app: a generous bowl of chicken biryani, distinct long grain basmati rice white and saffron gold, cooked chicken pieces, caramelized onions, a few mint leaves. Three-quarter overhead composition, entire bowl centered on a simple dark warm tabletop, soft natural window light, appetizing realistic textures. Landscape composition, no writing, no people, no logos, no watermarks. This is an illustrative synthetic sample image, not a photograph of a specific real recipe.

### Chana masala

Photorealistic editorial food illustration for an offline sample Indian recipe app: a generous bowl of chana masala, chickpeas coated in rich tomato and spice gravy, fresh cilantro garnish, small subtle ginger slivers. Three-quarter overhead composition, entire bowl centered on a simple dark warm tabletop, soft natural window light, appetizing realistic textures. Landscape composition, no writing, no people, no logos, no watermarks. This is an illustrative synthetic sample image, not a photograph of a specific real recipe.
