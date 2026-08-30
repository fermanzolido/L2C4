Custom buy lists go here.

BuyListData reads this folder when CustomBuyListLoad is enabled in General.ini,
which it is by default. The folder shipped missing, so every start logged
"Directory not found" for it. An xml dropped in here is picked up alongside the
ones in the parent folder and overrides a buy list with the same id.
