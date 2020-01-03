package com.paradox_challenges.eu4_generator.format;

import com.paradox_challenges.eu4_generator.savegame.ArrayNode;
import com.paradox_challenges.eu4_generator.savegame.KeyValueNode;
import com.paradox_challenges.eu4_generator.savegame.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class NodeTransformer {

    public abstract Node transformNode(Node node);

}
