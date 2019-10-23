#   Manufacry
Bringing more manufacturing and factory components to
[Mindustry](https://mindustrygame.github.io/).

##### Note
Manufacry is a **code** mod for Mindustry and therefor needs a custom build
 (bleeding edge builds currently will not work).
##### Releases
Check releases page (desktop version only).
##  Features
   - [ ] Configurable crafters - Crafters where you can choose what to manufacture.
   - [ ] Addon modules - Blocks that enhance or modify the functionality of related crafters.
   - [ ] [Power production](#power-production) - Power generation using steam
   - [ ] [Oil distillation](#oil-distillation) - Use vanilla oil to produce
    more refined oils.
   - [ ] [Factories](#factories) - Produce new items/liquids
   - [ ] [Workforce](#workforce) - New aircraft and mechs for utility/defense/offence 
   - [ ] [Logic System](#logic-system) - Basic logic circuit components.
   
### Power production
- Steam Power
    - Steam Pressure
        - Low pressure steam - moderate heat
        - High pressure steam - high heat (causes damage on contact)
    - Steam Boilers
        - Outputs steam (used for power and oil distillation)
        - Inputs
            - Water (vanilla)
            - Distilled water (more efficient)
        - Types
            - Solid Fuelled - Burns items (similar to vanilla steam generator)
                - Coal - produces low pressure
                - Blast - produces low pressure
                - Pyrite - produces high pressure
            - Liquid Fuelled - Burns liquid fuel
                - Crude Oil	(vanilla) - produces low pressure
                - Fuel Oil	(distillation) - produces high pressure
    - Steam Turbine
        - Uses steam for power
        - Produces distilled water output
        - Pressure
            - Low - less power, less distilled water
            - High - more power, more distilled water
- Gas Power
    - Gas Turbine - Use natural gas for power.

### Oil Distillation
Inspiration from oil refining in other games/mods and also from the image below.

![Test](https://1.bp.blogspot.com/-TSvZKkzZ9mY/V9PhdzD344I/AAAAAAAAAGI/3B3Q4EkLhAMfY6es3CeUYzzDTxlWGEavACLcB/s1600/oil-distillation.gif)
(Image from [here](http://allabtinstru.blogspot.com/2016/09/ProcessofRefiningCrudeOil.html))
- Distillation Tower - Used in the refining of crude oil
    - Outputs
        - Lubricant - Used as a booster for new crafters (less power/less craft time)
        - Fuel Oil - Used in liquid fuelled boilers for high pressure steam
- Cracking Unit - Addon for distillation tower
    - Additional outputs
        - Kerosene - Used for new unit fuelling
        - Propane - Used for power generation
        - Diesel - Used for new unit fuelling        

### Factories
- TODO
    - Items
        - Vanilla upgrades
            - Copper (wires, plates)
            - Lead (batteries?)
            - Graphite (TODO)
            - Titanium (rods?, plates)
            - Thorium (rods, plates)
            - Silicon (circuits)
            - Plastanium (TODO)
            - Phase Fabric (TODO)
            - Surge Alloy (rods, plates)
            - Spore Pod (TODO)
            - Sand (?)
            - Blast/Pyratitem
            - Metaglass (capsules/containers)

### Workforce
- General
    - New units require fuel to operate
    - Refuelling stations - requires petrol
        - Dedicated stations or also smaller slower filling at factory.
- Roles
    - Utility
        - New miner with drop-off at factory or specific storage block.
    - Defence
        - TODO
    - Offence
        - TODO

### Logic System
- Basic
    - Wire with on/off signal
    - Gates (AND, OR, NOT)
    - Readers
        - Inventory reader (signal based on item count)
        - Tank reader (signal based on liquid count)
        - Power reader (signal based on power input/output/usage)
