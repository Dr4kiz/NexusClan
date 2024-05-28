package me.dkz.dev.nexusclan.database;

import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.manager.ChunkManager;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.nexus.NexusCrystal;
import me.dkz.dev.nexusclan.nexus.NexusMember;
import me.dkz.dev.nexusclan.nexus.NexusTag;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class SQLite {

    private Connection connection;
    private File databaseFile;
    private final Main plugin = Main.getInstance();
    private final ChunkManager chunkManager = plugin.getChunkManager();
    public void createDatabase() {
        this.databaseFile = new File(plugin.getDataFolder(), "nexusclan.db");
        try {
            if (!databaseFile.exists()) databaseFile.createNewFile();
            createTables();
        } catch (IOException | SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Nao foi possivel criar o banco de dados: {0}", e.getMessage());
        }
    }

    public void createTables() throws SQLException {
        openConnection();
        try (PreparedStatement statement = connection.prepareStatement("PRAGMA foreign_keys = ON;")) {
            statement.execute();
        }
        ;

        String tableMembers = "CREATE TABLE IF NOT EXISTS nexusMembers (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    clan_id INTEGER NOT NULL," +
                "    uuid TEXT NOT NULL UNIQUE," +
                "    energy REAL NOT NULL," +
                "    tag INT NOT NULL," +
                "    FOREIGN KEY (clan_id) REFERENCES nexusClans(id) ON DELETE CASCADE" +
                ");";

        String tableClans = "CREATE TABLE IF NOT EXISTS nexusClans (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    name TEXT NOT NULL UNIQUE," +
                "    tag VARCHAR(3) NOT NULL," +
                "    description TEXT NOT NULL" +
                ");";


        String tableNexus = "CREATE TABLE IF NOT EXISTS nexusCrystals ("+
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    clan_id INT NOT NULL UNIQUE," +
                "    world TEXT NOT NULL,"+
                "    posX double NOT NULL,"+
                "    posY double NOT NULL,"+
                "    posZ double NOT NULL,"+
                "    FOREIGN KEY (clan_id) REFERENCES nexusClans(id) ON DELETE CASCADE" +
                ");";

        String tableClaims = "CREATE TABLE IF NOT EXISTS nexusClaims ("+
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    clan_name TEXT NOT NULL," +
                "    world TEXT NOT NULL,"+
                "    posX INT NOT NULL,"+
                "    posZ INT NOT NULL,"+
                "    FOREIGN KEY (clan_name) REFERENCES nexusClans(name) ON DELETE CASCADE" +
                ");";

        try (PreparedStatement statement = connection.prepareStatement(tableClans)) {
            statement.execute();
        }


        try (PreparedStatement statement = connection.prepareStatement(tableMembers)) {
            statement.execute();
        }
        try (PreparedStatement statement = connection.prepareStatement(tableNexus)) {
            statement.execute();
        }
        try (PreparedStatement statement = connection.prepareStatement(tableClaims)) {
            statement.execute();
        }


    }

    public void deleteClan(NexusClan clan) {
        String clanSql = "DELETE FROM nexusClans where name = ?;";
        openConnection();
        try (PreparedStatement deleteStatement = connection.prepareStatement(clanSql)) {
            deleteStatement.setString(1, clan.getName());
            deleteStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void deleteMember(NexusMember member){
        String clanSql = "DELETE FROM nexusMembers where uuid = ?;";
        openConnection();
        try (PreparedStatement deleteStatement = connection.prepareStatement(clanSql)) {
            deleteStatement.setString(1, member.getPlayer().getUniqueId().toString());
            deleteStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int saveClan(NexusClan clan) {
        String insertSql = "INSERT INTO nexusClans (name, tag, description) VALUES (?, ?, ?);";
        String updateSql = "UPDATE nexusClans SET tag = ?, description = ? WHERE name = ?;";

        int clanID = -1;
        openConnection();
        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement.setString(1, clan.getName());
            insertStatement.setString(2, clan.getTag());
            insertStatement.setString(3, String.join(" ", clan.getDescription()));

            int affectedRows = insertStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating clan failed, no rows affected.");
            }

            try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    clanID = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating clan failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
                try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                    updateStatement.setString(1, clan.getTag());
                    updateStatement.setString(2, String.join(" ", clan.getDescription()));
                    updateStatement.setString(3, clan.getName());

                    int affectedRows = updateStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Updating clan failed, no rows affected.");
                    }

                    // Fetch the existing ID
                    String getIdSql = "SELECT id FROM nexusClans WHERE name = ?";
                    try (PreparedStatement getIdStatement = connection.prepareStatement(getIdSql)) {
                        getIdStatement.setString(1, clan.getName());
                        try (ResultSet resultSet = getIdStatement.executeQuery()) {
                            if (resultSet.next()) {
                                clanID = resultSet.getInt("id");
                            } else {
                                throw new SQLException("Updating clan failed, no ID obtained.");
                            }
                        }
                    }
                }catch (SQLException ex){
                    System.out.println("Error SQL: "+ex.getMessage());
                }

        }

        return clanID;
    }


    public void saveMember(NexusMember member, int clanId) {
        String insertSql = "INSERT INTO nexusMembers (uuid, clan_id, energy, tag) VALUES (?, ?, ?, ?);";
        String updateSql = "UPDATE nexusMembers SET clan_id = ?, energy = ?, tag = ? WHERE uuid = ?;";

        openConnection();
        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            insertStatement.setString(1, member.getPlayer().getUniqueId().toString());
            insertStatement.setInt(2, clanId);
            insertStatement.setDouble(3, member.getEnergy());
            insertStatement.setInt(4, member.getTag().getHierarchy());


                int affectedRows = insertStatement.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating member failed, no rows affected.");
                }
        } catch (SQLException e) {
            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setInt(1, clanId);
                updateStatement.setDouble(2, member.getEnergy());
                updateStatement.setInt(3, member.getTag().getHierarchy());
                updateStatement.setString(4, member.getPlayer().getUniqueId().toString());

                int affectedRows = updateStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Updating member failed, no rows affected.");
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void saveChunks(Set<Chunk> claimedChunks, String clanName){
        String insertSql = "INSERT INTO nexusClaims (clan_name, world, posX, posZ) VALUES (?, ?, ?, ?);";
        openConnection();
        if(claimedChunks != null){
            claimedChunks.forEach(chunk -> {

                String world = chunk.getWorld().getName();
                int x = chunk.getX();
                int z = chunk.getZ();

                try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                    insertStatement.setString(1, clanName);
                    insertStatement.setString(2, world);
                    insertStatement.setInt(3, x);
                    insertStatement.setInt(4, z);
                    int affectedRows = insertStatement.executeUpdate();

                    if (affectedRows == 0) {
                        throw new SQLException("Creating nexus claims failed, no rows affected.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

    }

    public void saveNexus(NexusCrystal crystal, int clanId) {
        String insertSql = "INSERT INTO nexusCrystals (clan_id, world, posX, posY, posZ) VALUES (?, ?, ?, ?, ?);";
        String updateSql = "UPDATE nexusCrystals SET world = ?, posX = ?, posY = ?, posZ = ? WHERE clan_id = ?;";

        openConnection();
        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            insertStatement.setInt(1, clanId);
            insertStatement.setString(2, crystal.getWorld().getWorld().getName());
            insertStatement.setDouble(3, crystal.locX);
            insertStatement.setDouble(4, crystal.locY);
            insertStatement.setDouble(5, crystal.locZ);

            int affectedRows = insertStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating nexus crystal failed, no rows affected.");
            }
        } catch (SQLException e) {
            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setString(1, crystal.getWorld().getWorld().getName());
                updateStatement.setDouble(2, crystal.locX);
                updateStatement.setDouble(3, crystal.locY);
                updateStatement.setDouble(4, crystal.locZ);
                updateStatement.setInt(5, clanId);

                int affectedRows = updateStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Updating nexus crystal failed, no rows affected.");
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }



    public Set<Chunk> getChunks(NexusClan clan){
        String claimsSql = "SELECT * FROM nexusClaims WHERE clan_name = ?;";
        Set<Chunk> chunks = new HashSet<>();
        try (PreparedStatement selectNexusStatement = connection.prepareStatement(claimsSql)) {
            selectNexusStatement.setString(1, clan.getName());
            try (ResultSet resultNexus = selectNexusStatement.executeQuery()) {
                while (resultNexus.next()) {
                    String world = resultNexus.getString("world");
                    int posX = resultNexus.getInt("posX");
                    int posY = resultNexus.getInt("posZ");
                    Chunk chunkAt = Bukkit.getWorld(world).getChunkAt(posX, posY);
                    chunks.add(chunkAt);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return chunks;
    }



    public List<NexusClan> getClans() {
        List<NexusClan> clans = new ArrayList<>();

        String clanSql = "SELECT * FROM nexusClans;";
        String memberSql = "SELECT * FROM nexusMembers WHERE clan_id = ?;";
        String nexusSql = "SELECT * FROM nexusCrystals WHERE clan_id = ?";

        openConnection();
        try (PreparedStatement selectClansStatement = connection.prepareStatement(clanSql);
             ResultSet resultClans = selectClansStatement.executeQuery()) {

            while (resultClans.next()) {
                int clanId = resultClans.getInt("id");
                String name = resultClans.getString("name");
                String clanTag = resultClans.getString("tag");
                String description = resultClans.getString("description");

                NexusClan nexusClan = new NexusClan();
                nexusClan.setName(name);
                nexusClan.setTag(clanTag);
                nexusClan.setDescription(Collections.singletonList(description));

                try (PreparedStatement selectMembersStatement = connection.prepareStatement(memberSql)) {
                    selectMembersStatement.setInt(1, clanId);
                    try (ResultSet resultMembers = selectMembersStatement.executeQuery()) {
                        while (resultMembers.next()) {
                            UUID uuid = UUID.fromString(resultMembers.getString("uuid"));
                            double energy = resultMembers.getDouble("energy");
                            int tag = resultMembers.getInt("tag");

                            NexusMember nexusMember = NexusMember.builder()
                                    .player(Bukkit.getPlayer(uuid))
                                    .energy(energy)
                                    .tag(NexusTag.values()[tag])
                                    .clan(nexusClan)
                                    .build();

                            nexusClan.getMembers().add(nexusMember);
                            if (nexusMember.getTag().equals(NexusTag.LEADER)) {
                                nexusClan.setOwner(nexusMember);
                            }
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                try (PreparedStatement selectNexusStatement = connection.prepareStatement(nexusSql)) {
                    selectNexusStatement.setInt(1, clanId);
                    try (ResultSet resultNexus = selectNexusStatement.executeQuery()) {
                        while (resultNexus.next()) {
                            String world = resultNexus.getString("world");
                            double posX = resultNexus.getDouble("posX");
                            double posY = resultNexus.getDouble("posY");
                            double posZ = resultNexus.getDouble("posZ");

                            Location location = new Location(Bukkit.getWorld(world), posX, posY, posZ);
                            World craftWorld  = ((CraftWorld)location.getWorld()).getHandle();
                            NexusCrystal crystal = new NexusCrystal(craftWorld, nexusClan);
                            crystal.locX = posX;
                            crystal.locY = posY;
                            crystal.locZ = posZ;
                            nexusClan.setNexusCrystal(crystal);
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                clans.add(nexusClan);
                chunkManager.addClaim(nexusClan, getChunks(nexusClan));

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return clans;
    }

    public Connection openConnection() {
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            } catch (ClassNotFoundException | SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Não foi possível criar a conexão com o banco de dados: {0}", e.getMessage());
            }
        }
        return connection;
    }

}
