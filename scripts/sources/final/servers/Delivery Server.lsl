/*
    Product Delivery Server

    The PDS is responsible for delivering products to customers.
    The PDS may be called by Harbinger to deliver a product when another product requests it as well. This may happen for some products that dispense no-copy items as well, or are no-copy themselves but request a update delivery.


    This file is a part of Harbinger
    https://github.com/AriasCreations/Harbinger
*/

#define PRESHAREDKEY "change_me"
#define CLIENT_NICK "Sample--changeme"
#include "../sources/raw/servers/delivery_server.lsl"