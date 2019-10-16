#   Manufacry
Bringing more manufacturing and factory components to
[Mindustry](https://mindustrygame.github.io/). 
##  Features
   - [ ] Configurable crafters - Crafters where you can choose what to
    manufacture.
   - [ ] [Steam power](#steam-power) - Power generation using steam
   - [ ] [Oil distillation](#oil-distillation) - Use vanilla oil to produce
    more refined oils.
   - [ ] [Factories](#factories) - Produce new items/liquids 
   - [ ] [Logic System](#logic-system) - Basic logic circuit components.
   
### Steam power
- Steam Pressure
    -	Low pressure steam - moderate heat
    -	High pressure steam - high heat (causes damage on contact)
- Steam boilers
    - Outputs steam (used for power and oil distillation)
    - Inputs
        - Water (vanilla)
        - Distilled water (more efficient)
    - Types
        - Solid fuelled - Burns items (similar to vanilla steam generator)
            - Coal - produces low pressure
            - Blast - produces low pressure
            - Pyrite - produces high pressure
        - Liquid fuelled - Burns liquid fuel
            - Oil	(vanilla) - produces low pressure
            - Fuel oil	(distillation) - produces high pressure
- Steam generator
    - Uses steam for power
    - Produces distilled water output
    - Pressure
        - Low - less power, less distilled water
        - High - more power, more distilled water
