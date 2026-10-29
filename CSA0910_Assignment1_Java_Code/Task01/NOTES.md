# Task 1 — Analysis prompts
- Inputs: registration number, vehicle category, user/pass status, requested entry/exit.
- Outputs: allocated floor/slot, ticket number, entry time, exit fee, errors when invalid/full.
- Candidate classes: Vehicle, ParkingSlot, ParkingFacility, ParkingTicket, User/Pass, PricingRule.
- Rules: one vehicle per slot; no duplicate active registration; ticket IDs are unique; invalid floor/slot rejected; only valid passes receive free/discounted parking.
- Complexity: searching an array of n vehicles is O(n); adding an attribute to objects generally changes the Vehicle class, while parallel arrays require keeping each array aligned.
