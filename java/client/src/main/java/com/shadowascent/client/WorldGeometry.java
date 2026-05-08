package com.shadowascent.client;

record WorldGeometry(
        float worldLeftX,
        float worldRightX,
        float floorY,
        float ceilingY,
        float windowWidth,
        float windowHeight,
        float playerRadius,
        float interactRadius,
        float hubRoomEndX,
        float forgeRoomEndX,
        float shaftRoomEndX,
        float summitRoomEndX
) {}
