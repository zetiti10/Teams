package fr.simple.teams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.simple.teams.assaut.Assaut;
import fr.simple.teams.assautRequest.AssautRequestData;
import fr.simple.teams.assautRequest.AssautRequestGUI;
import fr.simple.teams.assautRequest.AssautRequestSendData;
import fr.simple.teams.claims.ClaimData;
import fr.simple.teams.claims.Claims;
import fr.simple.teams.claims.ClaimsManager;
import fr.simple.teams.commands.TeamCommandControlGUI;
import fr.simple.teams.commands.TeamCommandCreateData;
import fr.simple.teams.commands.TeamCommandCreateGUI;
import fr.simple.teams.commands.TeamCommandInfoGUI;
import fr.simple.teams.functionsGUI.GUIInterface;
import net.md_5.bungee.api.ChatColor;

public class TeamListener implements Listener {

	private Teams teams;

	public TeamListener(Teams teams) {
		this.teams = teams;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.CHEST) {
			Block block = event.getBlock();
			try {
				String customName = ((Chest) block.getState()).getCustomName();
				if (customName.startsWith("banque")) {
					Player player = event.getPlayer();
					String team = TeamData.getPlayerTeam(player.getUniqueId());
					if (!team.equals("null")) {
						List<String> teamsInFight = Assaut.teamsInFight2;
						for (int i = 0; i < teamsInFight.size(); i++) {
							String current = teamsInFight.get(i);
							String[] words = current.split("\\s+");
							String attaquants = words[0];
							String d??fenceurs = words[1];
							if (team.equals(attaquants) || team.equals(d??fenceurs)) {
								return;
							}
						}
						if (TeamData.getBankChest(team) != null) {
							if (TeamData.getPlayerRank(player.getUniqueId()).equals("chefs")) {
								TeamData.setBankChest(team, null);
								player.sendMessage(teams.prefix
										+ " ??9Le coffre banque de botre team a ??t?? enlev??. Replacez-le ailleurs.");
								return;

							} else {
								event.setCancelled(true);
								player.sendMessage(teams.prefix
										+ " ??cVous devez ??tre chef de la team pour pouvoir casser le coffre banque de la team.");
								return;
							}
						}
					}
				}

			} catch (NullPointerException e) {
				return;
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getBlock().getType() == Material.CHEST) {
			if (event.getItemInHand().getItemMeta().getDisplayName().startsWith("banque")) {
				Player player = event.getPlayer();
				String team = TeamData.getPlayerTeam(player.getUniqueId());
				if (team != "null") {
					String teamChest = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName()
							.replace("banque ", ""); // \\ s +
					if (team.equals(teamChest)) {
						if (TeamData.getBankChest(team) != null) {
							Location chestLoc = TeamData.getBankChest(team);
							player.sendMessage(teams.prefix
									+ " ??cLe coffre banque de votre team existe d??j??, utilisez celui-ci (il se situe en ??c??l"
									+ chestLoc.getX() + "??c, ??c??l" + chestLoc.getY() + "??c, ??c??l" + chestLoc.getZ()
									+ "??c).");
							event.setCancelled(true);
							for (ItemStack i : player.getInventory().getContents()) {
								if (i != null && i.getType() == Material.CHEST && i.hasItemMeta()
										&& i.getItemMeta().hasDisplayName()
										&& i.getItemMeta().getDisplayName().startsWith("banque")) {
									player.getInventory().remove(i);
								}
							}
							return;
						} else {
							for (int x = 0; x < Claims.claimsList.size(); x++) {
								if (Claims.claimsList.get(x).isInArea(event.getBlock().getLocation())) {
									if (Claims.claimsList.get(x).getName()
											.equals(TeamData.getPlayerTeam(player.getUniqueId()))) {
										if (TeamData.getPlayerRank(player.getUniqueId()).equals("chefs")) {
											player.sendMessage(teams.prefix
													+ " ??9Bravo ! Vous avez enfin pos?? votre coffre banque de team ! Mettez-y dedans toutes vos ressources les plus pr??cieuses : minerais...");
											TeamData.setBankChest(team, event.getBlock().getLocation());
											return;
										} else {
											for (ItemStack i : player.getInventory().getContents()) {
												if (i != null && i.getType() == Material.CHEST && i.hasItemMeta()
														&& i.getItemMeta().hasDisplayName()
														&& i.getItemMeta().getDisplayName().startsWith("banque")) {
													player.getInventory().remove(i);
												}
											}
											player.sendMessage(teams.prefix
													+ " ??cVous n'avez pas la permission de poser le coffre banque de team, vous devez ??tre chef de la team !");
											event.setCancelled(true);
											return;
										}
									}
								}
							}

							player.sendMessage(teams.prefix
									+ " ??cVous devez poser votre coffre banque de team dans votre rep??re !");
							event.setCancelled(true);
							return;

						}
					} else {
						for (ItemStack i : player.getInventory().getContents()) {
							if (i != null && i.getType() == Material.CHEST && i.hasItemMeta()
									&& i.getItemMeta().hasDisplayName()
									&& i.getItemMeta().getDisplayName().startsWith("banque")) {
								player.getInventory().remove(i);
							}
						}
						player.sendMessage(
								teams.prefix + " ??cVous devez ??tre dans une team pour pouvoir utiliser ce coffre !");
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler
	public void onPVP(EntityDamageByEntityEvent event) {

	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {

	}

	List<String> playersTeamName = new ArrayList<String>();
	List<String> playersTeamDesc = new ArrayList<String>();

	List<String> playersTeamReDesc = new ArrayList<String>();

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		for (int i = 0; i < playersTeamName.size(); i++) {
			if (event.getPlayer().getName().equals(playersTeamName.get(i))) {
				Player player = event.getPlayer();
				if (event.getMessage().contains(" ")) {
					player.sendMessage(teams.prefix + " ??cLe nom de team ne peut contenir d'espace(s) !");
					player.sendMessage(teams.prefix + " ??9R??essayez.");
					event.setCancelled(true);
					return;
				}

				playersTeamName.remove(i);
				player.sendMessage(teams.prefix + " ??9Nouveau nom de team : ??b" + event.getMessage() + "??9.");
				player.sendMessage(teams.prefix + " ??9Tapez de nouveau ??b/team create ??9pour rouvrir le menu.");
				TeamCommandCreateData tccd = TeamCommandCreateData.TeamCommandCreateData.get(player.getName());
				tccd.TeamCommandCreateDataSetName(event.getMessage());
				event.setCancelled(true);
				return;

			}
		}

		for (int i = 0; i < playersTeamDesc.size(); i++) {
			if (event.getPlayer().getName().equals(playersTeamDesc.get(i))) {
				Player player = event.getPlayer();
				playersTeamDesc.remove(i);
				player.sendMessage(teams.prefix + " ??9Nouvelle description de team : ??b" + event.getMessage() + "??9.");
				player.sendMessage(teams.prefix + " ??9Tapez de nouveau ??b/team create ??9pour rouvrir le menu.");
				TeamCommandCreateData tccd = TeamCommandCreateData.TeamCommandCreateData.get(player.getName());
				tccd.TeamCommandCreateDataSetSlogan(event.getMessage());
				event.setCancelled(true);
				return;

			}
		}

		for (int i = 0; i < playersTeamReDesc.size(); i++) {
			if (event.getPlayer().getName().equals(playersTeamReDesc.get(i))) {
				event.setCancelled(true);
				Player player = event.getPlayer();
				playersTeamReDesc.remove(i);
				if (TeamData.getPlayerRank(player.getUniqueId()) == "chefs") {
					TeamData.setSlogan(TeamData.getPlayerTeam(player.getUniqueId()), event.getMessage());
					player.sendMessage(
							teams.prefix + " ??9Nouvelle description de team : ??b" + event.getMessage() + "??9.");
					player.sendMessage(teams.prefix + " ??9Tapez de nouveau ??b/team control ??9pour rouvrir le menu.");
					TeamCommandControlGUI.controlTeamGUIResetMain(player);
					return;
				} else {
					player.sendMessage(
							teams.prefix + " ??cVous n'avez pas la permission de changer la description de la team !");
					TeamCommandControlGUI.controlTeamGUIResetMain(player);
					return;
				}

			}

		}

	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onClick(InventoryClickEvent event) {

		if (event.getCurrentItem() == null) {
			return;
		}

		ItemStack current = event.getCurrentItem();
		Player player = (Player) event.getWhoClicked();

		if (event.getInventory() instanceof AnvilInventory) {
			if (event.getRawSlot() == 2) {
				ItemMeta meta = current.getItemMeta();
				if (meta != null) {
					if (meta.hasDisplayName()) {
						String newName = meta.getDisplayName();
						if (newName.startsWith("banque")) {

							UUID uuid = player.getUniqueId();

							if (TeamData.getPlayerTeam(uuid) != "null") {
								if (TeamData.getPlayerRank(uuid) == "chefs") {
									if (TeamData.getBankChest(TeamData.getPlayerTeam(player.getUniqueId())) == null) {
										String[] words = newName.split("\\s+");
										if (words[1] != null) {
											String teamName = words[1];
											if (TeamData.getPlayerTeam(uuid).equals(teamName)) {
												player.sendMessage(teams.prefix
														+ " ??9Le coffre banque de votre team a ??t?? cr??e ! Placez le dans l'endroit le plus s??curis?? de votre rep??re, car si vous vous le faites voler lors d'un assaut, l'??quipe advairse aura gagn??. Mettez-y dedans toutes vos ressources les plus pr??cieuses : minerais...");
												return;
											} else {
												event.setCancelled(true);
												player.closeInventory();
												player.sendMessage(teams.prefix
														+ " ??cLe nom de cette team n'existe pas, r??essayez comme ceci : ??c??lbanque <nom de votre team>??c.");
												return;
											}
										} else {
											event.setCancelled(true);
											player.closeInventory();
											player.sendMessage(teams.prefix
													+ " ??cVous devez sp??cifier le nom de votre team comme ceci : ??c??lbanque <nom de votre team> ??c!");
											return;
										}
									} else {
										Location chestLoc = TeamData.getBankChest(TeamData.getPlayerTeam(uuid));
										event.setCancelled(true);
										player.closeInventory();
										player.sendMessage(teams.prefix
												+ " ??cLe coffre banque de votre team existe d??j??, utilisez celui-ci (il se situe en ??c??l"
												+ chestLoc.getX() + "??c, ??c??l" + chestLoc.getY() + "??c, ??c??l"
												+ chestLoc.getZ() + "??c).");
										return;
									}
								} else {
									event.setCancelled(true);
									player.closeInventory();
									player.sendMessage(teams.prefix
											+ " ??cSeul un des chefs de la team peut cr??er un coffre banque de team !");
									return;
								}
							} else {
								event.setCancelled(true);
								player.closeInventory();
								player.sendMessage(teams.prefix
										+ " ??cVous ne pouvez pas renommer un coffre en commen??ant par ??c??lbanque??c, ceci est r??serv?? aux teams.");
								return;
							}

						}
					}
				}
			}
		}

		switch (event.getView().getTitle()) {
		case "??9Cr??er une team":
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			TeamCommandCreateData tccd = TeamCommandCreateData.TeamCommandCreateData.get(player.getName());
			event.setCancelled(true);
			switch (current.getType()) {

			case NAME_TAG:
				playersTeamName.add(player.getName());
				player.closeInventory();
				player.sendMessage(teams.prefix + " ??9Entrez un nom de team.");
				break;
			case PAPER:
				playersTeamDesc.add(player.getName());
				player.closeInventory();
				player.sendMessage(teams.prefix + " ??9Entrez une description de team.");
				break;
			case WHITE_WOOL:
				TeamCommandCreateGUI.createTeamGUIColor(player, tccd);
				break;
			case OAK_DOOR:
				TeamCommandCreateGUI.createTeamGUIAccess(player, tccd);
				break;
			case RED_CONCRETE:
				player.closeInventory();
				tccd.TeamCommandCreateDataSetName("null");
				tccd.TeamCommandCreateDataSetSlogan("null");
				tccd.TeamCommandCreateDataSetColor("WHITE");
				tccd.TeamCommandCreateDataSetAccess(TeamAcces.OPEN);
				break;
			case GREEN_CONCRETE:
				if (tccd.TeamCommandCreateDataGetName() == "null" || tccd.TeamCommandCreateDataGetSlogan() == "null") {
					player.sendMessage(
							teams.prefix + " ??cVous devez choisir un nom de team ainsi qu'une description !");
					return;
				}
				TeamCommandCreateGUI.createTeamGUIConfirm(player, tccd);
				break;
			default:
				break;
			}
			break;
		case "??9Choix de la couleur":
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			TeamCommandCreateData tccd1 = TeamCommandCreateData.TeamCommandCreateData.get(player.getName());
			event.setCancelled(true);
			switch (current.getType()) {
			case REDSTONE:
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd1);
				break;
			case YELLOW_DYE:
				tccd1.TeamCommandCreateDataSetColor("YELLOW");
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd1);
				break;
			case ORANGE_DYE:
				tccd1.TeamCommandCreateDataSetColor("GOLD");
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd1);
				break;
			case RED_DYE:
				tccd1.TeamCommandCreateDataSetColor("RED");
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd1);
				break;
			case PINK_DYE:
				tccd1.TeamCommandCreateDataSetColor("LIGHT_PURPLE");
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd1);
				break;
			case PURPLE_DYE:
				tccd1.TeamCommandCreateDataSetColor("DARK_PURPLE");
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd1);
				break;
			case GREEN_DYE:
				tccd1.TeamCommandCreateDataSetColor("DARK_GREEN");
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd1);
				break;
			case LIME_DYE:
				tccd1.TeamCommandCreateDataSetColor("GREEN");
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd1);
				break;

			case BLUE_DYE:
				tccd1.TeamCommandCreateDataSetColor("DARK_BLUE");
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd1);
				break;
			case LIGHT_BLUE_DYE:
				tccd1.TeamCommandCreateDataSetColor("BLUE");
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd1);
				break;
			case GRAY_DYE:
				tccd1.TeamCommandCreateDataSetColor("GRAY");
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd1);
				break;
			default:
				break;
			}
			break;
		case "??9Choix de l'acc??s":
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			TeamCommandCreateData tccd11 = TeamCommandCreateData.TeamCommandCreateData.get(player.getName());
			event.setCancelled(true);
			switch (current.getType()) {
			case REDSTONE:
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd11);
				break;
			case IRON_DOOR:
				tccd11.TeamCommandCreateDataSetAccess(TeamAcces.PRIVATE);
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd11);
				break;
			case SPRUCE_DOOR:
				tccd11.TeamCommandCreateDataSetAccess(TeamAcces.ON_INVITATION);
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd11);
				break;
			case ACACIA_DOOR:
				tccd11.TeamCommandCreateDataSetAccess(TeamAcces.OPEN);
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd11);
				break;
			default:
				break;
			}
			break;
		case "??9Confirmation":
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			TeamCommandCreateData tccd111 = TeamCommandCreateData.TeamCommandCreateData.get(player.getName());
			event.setCancelled(true);
			switch (current.getType()) {
			case RED_CONCRETE:
				TeamCommandCreateGUI.createTeamGUIMain(player, tccd111);
				break;
			case GREEN_CONCRETE:

				List<String> allTeams = TeamData.getAllTeams();
				for (int i = 0; i < allTeams.size(); i++) {
					if (allTeams.get(i).equals(tccd111.TeamCommandCreateDataGetName())) {
						player.sendMessage(
								teams.prefix + " ??cUne team portant ce nom existe d??j??, veuillez changer son nom.");
						TeamCommandCreateGUI.createTeamGUIMain(player, tccd111);
						return;
					}
				}

				player.closeInventory();

				double createTeamPrice = 500000;
				if (teams.eco.getBalance(player) < createTeamPrice) {
					double i = createTeamPrice - teams.eco.getBalance(player);
					player.sendMessage(teams.prefix
							+ " ??cVous n'avez pas assez d'argent pour cr??er une team ! Il vous manque ??c??l " + i
							+ " ??ceuros.");
					return;
				}

				teams.eco.withdrawPlayer(player, createTeamPrice);

				TeamCreate.CreateATeam(player, player.getUniqueId(), tccd111.TeamCommandCreateDataGetName(),
						tccd111.TeamCommandCreateDataGetSlogan(), tccd111.TeamCommandCreateDataGetAccess().toString(),
						tccd111.TeamCommandCreateDataGetColor());
				String team = tccd111.TeamCommandCreateDataGetName();
				player.sendMessage(teams.prefix + " ??9La team ??b" + team + " ??9a bien ??t?? cr??e, bravo !");
				Bukkit.broadcastMessage(teams.prefix + " ??b" + player.getName() + " ??9 a cr??e la team "
						+ ChatColor.valueOf(TeamData.getColor(team)) + team + " ??9, avec pour description ??b"
						+ TeamData.getSlogan(team) + " ??9et en acc??s " + TeamData.getAccess(team) + " ??9.");

				tccd111.TeamCommandCreateDataSetName("null");
				tccd111.TeamCommandCreateDataSetSlogan("null");
				tccd111.TeamCommandCreateDataSetColor("WHITE");
				tccd111.TeamCommandCreateDataSetAccess(TeamAcces.OPEN);

				break;
			default:
				break;
			}
			break;
		case "??9Contr??le de la team":
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			event.setCancelled(true);
			switch (current.getType()) {
			case PLAYER_HEAD:
				if (TeamData.getPlayerRank(player.getUniqueId()) == "chefs"
						|| TeamData.getPlayerRank(player.getUniqueId()) == "sous-chefs") {
					TeamCommandControlGUI.controlTeamGUIPlayersMain(player);
				} else {
					player.sendMessage(teams.prefix + " ??cVous ne pouvez pas g??rer les joueurs !");
				}
				break;
			case DIAMOND_SWORD:
				break;
			case REPEATER:
				if (TeamData.getPlayerRank(player.getUniqueId()) == "chefs") {
					TeamCommandControlGUI.controlTeamGUIResetMain(player);
				} else {
					player.sendMessage(teams.prefix + " ??cVous ne pouvez pas ??diter la team !");
				}
				break;
			case GOLDEN_HOE:
				if (TeamData.getPlayerRank(player.getUniqueId()) == "chefs") {
					ClaimsManager.claimersList.add(player);
					player.closeInventory();
					player.sendMessage(teams.prefix
							+ " ??9Pour cr??er le claim : utilisez la houe en or pour faire un clique gauche aux deux coins oppos??s du claim souhait?? pour qu'il se cr??e.");
					player.getInventory()
							.addItem(GUIInterface.newItemC(Material.GOLDEN_HOE, "??bOutil de claim", 1,
									Arrays.asList("??9", "??9Permet de claim un", "??9rep??re de team", "??9",
											"??9Clique gauche sur deux", "??9coins oppos??s pour cr??er", "??9le claim"),
									true));
				} else {
					player.sendMessage(teams.prefix + " ??cVous ne pouvez pas d??finir le claim !");
				}
				break;
			case REDSTONE:
				player.closeInventory();
				break;
			default:
				break;
			}
			break;
		case "??9Contr??le des joueurs":
			event.setCancelled(true);
			switch (current.getType()) {
			case PLAYER_HEAD:
				String playerName = current.getItemMeta().getDisplayName();
				if (!playerName.equals(player.getName())) {
					playerName = playerName.replace("??b", "");
					TeamCommandControlGUI.controlTeamGUIPlayersPlayer(player, playerName);
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
				} else {
					player.sendMessage(teams.prefix + " ??cVous ne pouvez pas vous g??rer-vous m??me.");
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				}
				break;
			case REDSTONE:
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
				TeamCommandControlGUI.controlTeamGUIMain(player);
				break;
			default:
				break;
			}
			break;
		case "??9Contr??le du joueur":
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			switch (current.getType()) {
			case BARRIER:
				String playerName = current.getItemMeta().getDisplayName();
				playerName = playerName.replace("??cExpulser??c??l ", "");
				TeamCommandControlGUI.controlTeamGUIPlayersConfirm(player, playerName);
				break;
			case COMMAND_BLOCK:
				String playerName1 = current.getItemMeta().getDisplayName();
				playerName1 = playerName1.replace("??bG??rer les permissions de??b??l ", "");
				TeamCommandControlGUI.controlTeamGUIPlayersPremission(player, playerName1);
				break;
			case REDSTONE:
				TeamCommandControlGUI.controlTeamGUIPlayersMain(player);
				break;
			default:
				break;
			}
			break;
		case "??9Confirmer l'expulsion":
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			switch (current.getType()) {
			case RED_CONCRETE:
				TeamCommandControlGUI.controlTeamGUIMain(player);
				break;
			case GREEN_CONCRETE:
				String playerName = current.getItemMeta().getDisplayName();
				playerName = playerName.replace("??aExpulser??a??l ", "");
				Player p = Bukkit.getPlayer(playerName);
				UUID uuid = p.getUniqueId();
				if (TeamData.getPlayerRank(player.getUniqueId()) == "chefs"
						&& (TeamData.getPlayerRank(uuid) == "sous-chefs"
								|| TeamData.getPlayerRank(uuid) == "membres-de-confiance"
								|| TeamData.getPlayerRank(uuid) == "membres")) {
					System.out.println("1");
					TeamData.removePlayer(TeamData.getPlayerTeam(p.getUniqueId()), uuid);
					player.sendMessage(teams.prefix + " ??b" + playerName + " ??9a bien ??t?? expuls?? de la team !");
					TeamCommandControlGUI.controlTeamGUIMain(player);
				} else if (TeamData.getPlayerRank(player.getUniqueId()) == "sous-chefs"
						&& (TeamData.getPlayerRank(uuid) == "membres-de-confiance"
								|| TeamData.getPlayerRank(uuid) == "membres")) {
					System.out.println("2");
					TeamData.removePlayer(TeamData.getPlayerTeam(p.getUniqueId()), uuid);
					player.sendMessage(teams.prefix + " ??b" + playerName + " ??9a bien ??t?? expuls?? de la team !");
					TeamCommandControlGUI.controlTeamGUIMain(player);
				} else if (player.getUniqueId().toString() == TeamData
						.getCreator(TeamData.getPlayerTeam(player.getUniqueId()))) {
					System.out.println("3");
					TeamData.removePlayer(TeamData.getPlayerTeam(p.getUniqueId()), uuid);
					player.sendMessage(teams.prefix + " ??b" + playerName + " ??9a bien ??t?? expuls?? de la team !");
					TeamCommandControlGUI.controlTeamGUIMain(player);
				} else {
					player.sendMessage(teams.prefix + " ??cVous n'avez pas la permission d'expulser ce joueur !");
					player.closeInventory();
				}
				break;
			default:
				break;
			}
			break;
		case "??9Permissions du joueur":
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

			switch (current.getType()) {
			case RED_CONCRETE:
				if (TeamData.getCreator(TeamData.getPlayerTeam(player.getUniqueId())) == player.getUniqueId()
						.toString()) {
					String playerName = current.getItemMeta().getDisplayName();
					playerName = playerName.replace("??cD??finir ??c??l", "");
					playerName = playerName.replace(" ??cchef", "");
					Player p = Bukkit.getPlayer(playerName);
					UUID uuid = p.getUniqueId();
					String team = TeamData.getPlayerTeam(uuid);
					TeamData.removePlayer(team, uuid);
					TeamData.addPlayer(team, uuid, "chefs");
					player.sendMessage(teams.prefix + " ??b " + p.getName() + " ??9est d??sormais ??bchef ??9de la team !");
					TeamCommandControlGUI.controlTeamGUIMain(player);
					break;
				} else {
					player.sendMessage(teams.prefix + " ??cVous n'avez pas la permission de faire ceci !");
					TeamCommandControlGUI.controlTeamGUIMain(player);
					break;
				}
			case ORANGE_CONCRETE:
				if (TeamData.getPlayerRank(player.getUniqueId()) == "chefs") {
					String playerName1 = current.getItemMeta().getDisplayName();
					playerName1 = playerName1.replace("??6D??finir ??6??l", "");
					playerName1 = playerName1.replace(" ??6sous chef", "");
					Player p1 = Bukkit.getPlayer(playerName1);
					UUID uuid1 = p1.getUniqueId();
					String team1 = TeamData.getPlayerTeam(uuid1);
					TeamData.removePlayer(team1, uuid1);
					TeamData.addPlayer(team1, uuid1, "sous-chefs");
					player.sendMessage(
							teams.prefix + " ??b " + p1.getName() + " ??9est d??sormais ??bsous chef ??9de la team !");
					TeamCommandControlGUI.controlTeamGUIMain(player);
					break;
				} else {
					player.sendMessage(teams.prefix + " ??cVous n'avez pas la permission de faire ceci !");
					TeamCommandControlGUI.controlTeamGUIMain(player);
					break;
				}
			case LIGHT_BLUE_CONCRETE:
				if (TeamData.getPlayerRank(player.getUniqueId()) == "chefs") {
					String playerName11 = current.getItemMeta().getDisplayName();
					playerName11 = playerName11.replace("??9D??finir ??9??l", "");
					playerName11 = playerName11.replace(" ??9membre de confiance", "");
					Player p11 = Bukkit.getPlayer(playerName11);
					UUID uuid11 = p11.getUniqueId();
					String team11 = TeamData.getPlayerTeam(uuid11);
					TeamData.removePlayer(team11, uuid11);
					TeamData.addPlayer(team11, uuid11, "membres-de-confiance");
					player.sendMessage(teams.prefix + " ??b " + p11.getName()
							+ " ??9est d??sormais ??bmembre de confiance ??9de la team !");
					TeamCommandControlGUI.controlTeamGUIMain(player);
					break;
				} else {
					player.sendMessage(teams.prefix + " ??cVous n'avez pas la permission de faire ceci !");
					TeamCommandControlGUI.controlTeamGUIMain(player);
					break;
				}
			case BLUE_CONCRETE:
				if (TeamData.getPlayerRank(player.getUniqueId()) == "chefs") {
					String playerName111 = current.getItemMeta().getDisplayName();
					playerName111 = playerName111.replace("??1D??finir ??1??l", "");
					playerName111 = playerName111.replace(" ??1membre", "");
					Player p111 = Bukkit.getPlayer(playerName111);
					UUID uuid111 = p111.getUniqueId();
					String team111 = TeamData.getPlayerTeam(uuid111);
					TeamData.removePlayer(team111, uuid111);
					TeamData.addPlayer(team111, uuid111, "membres");
					player.sendMessage(
							teams.prefix + " ??b " + p111.getName() + " ??9est d??sormais ??bmembre ??9de la team !");
					TeamCommandControlGUI.controlTeamGUIMain(player);
					break;
				} else {
					player.sendMessage(teams.prefix + " ??cVous n'avez pas la permission de faire ceci !");
					TeamCommandControlGUI.controlTeamGUIMain(player);
					break;
				}
			case REDSTONE:
				TeamCommandControlGUI.controlTeamGUIPlayersMain(player);
				break;
			default:
				break;
			}
			break;
		case "??9Modifier la team":
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			switch (current.getType()) {
			case NAME_TAG:
				player.sendMessage(teams.prefix
						+ " ??cIl est impossible de changer le nom de la team ! C'est cela qui la rend unique ( en fait, c'est surtout que je sais pas comment faire pour le changer, mais ??a il faut pas le dire x) -zetiti10, d??veloppeur de Simple, le plugin de teams ).");
				break;
			case PAPER:
				if (TeamData.getPlayerRank(player.getUniqueId()) == "chefs") {
					playersTeamReDesc.add(player.getName());
					player.closeInventory();
					player.sendMessage(teams.prefix + " ??9Entrez une nouvelle description de team.");
				} else {
					player.sendMessage(teams.prefix + " ??cVous ne pouvez pas changer les param??tres de la team !");
				}
				break;
			case OAK_DOOR:
				if (TeamData.getPlayerRank(player.getUniqueId()) == "chefs") {
					TeamCommandControlGUI.ControlTeamGUIResetAccess(player);
				} else {
					player.sendMessage(teams.prefix + " ??cVous ne pouvez pas changer les param??tres de la team !");
				}
				break;
			case WHITE_WOOL:
				if (TeamData.getPlayerRank(player.getUniqueId()) == "chefs") {
					TeamCommandControlGUI.controlTeamGUIResetColor(player);
				} else {
					player.sendMessage(teams.prefix + " ??cVous ne pouvez pas changer les param??tres de la team !");
				}
				break;
			case REDSTONE:
				TeamCommandControlGUI.controlTeamGUIMain(player);
				break;
			default:
				break;
			}
			break;
		case "??9Changer la couleur de la team":
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			String team = TeamData.getPlayerTeam(player.getUniqueId());
			switch (current.getType()) {
			case REDSTONE:
				TeamCommandControlGUI.controlTeamGUIResetMain(player);
				break;
			case YELLOW_DYE:
				TeamData.setColor(team, "YELLOW");
				break;
			case ORANGE_DYE:
				TeamData.setColor(team, "GOLD");
				break;
			case RED_DYE:
				TeamData.setColor(team, "RED");
				break;
			case PINK_DYE:
				TeamData.setColor(team, "LIGHT_PURPLE");
				break;
			case PURPLE_DYE:
				TeamData.setColor(team, "DARK_PURPLE");
				break;
			case GREEN_DYE:
				TeamData.setColor(team, "DARK_GREEN");
				break;
			case LIME_DYE:
				TeamData.setColor(team, "GREEN");
				break;
			case BLUE_DYE:
				TeamData.setColor(team, "DARK_BLUE");
				break;
			case LIGHT_BLUE_DYE:
				TeamData.setColor(team, "BLUE");
				break;
			case GRAY_DYE:
				TeamData.setColor(team, "GRAY");
				break;
			default:
				break;
			}
			TeamCommandControlGUI.controlTeamGUIResetMain(player);
			player.sendMessage(teams.prefix + " ??9Nouvelle couleur de team : "
					+ ChatColor.valueOf(TeamData.getColor(team)) + TeamData.getColor(team) + "??9.");
			break;
		case "??9Changer l'acc??s de la team":
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			String team1 = TeamData.getPlayerTeam(player.getUniqueId());
			switch (current.getType()) {
			case REDSTONE:
				TeamCommandControlGUI.controlTeamGUIResetMain(player);
				break;
			case IRON_DOOR:
				TeamData.setAccess(team1, "PRIVATE");
				break;
			case SPRUCE_DOOR:
				TeamData.setAccess(team1, "ON_INVITATION");
				break;
			case ACACIA_DOOR:
				TeamData.setAccess(team1, "OPEN");
				break;
			default:
				break;
			}
			TeamCommandControlGUI.controlTeamGUIResetMain(player);
			player.sendMessage(teams.prefix + " ??9Nouvel acc??s de la team : ??b" + TeamData.getAccess(team1) + " ??9.");
			break;

		case "??9Info sur la team":
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			switch (current.getType()) {
			case DIAMOND_SWORD:
				String TName = current.getItemMeta().getDisplayName();
				TName = TName.replace("??bClassement de la team ", "");
				Bukkit.dispatchCommand(player, "team top");
				break;
			case REDSTONE:
				player.closeInventory();
				break;
			case TNT:
				String TName1 = current.getItemMeta().getDisplayName();
				TName1 = TName1.replace("??bEnvoyer une demande d'assaut ?? la team ", "");
				try {
					AssautRequestData data = AssautRequestData.AssautRequestData.get(player.getName());
					data.setAttaquants(TeamData.getPlayerTeam(player.getUniqueId()));
					data.setDemandeur(player);
					data.setD??fenceurs(TName1);
					AssautRequestGUI.assautRequestGUIMain(player);
				} catch (NullPointerException e) {
					AssautRequestData data = new AssautRequestData(player.getName());
					data.setAttaquants(TeamData.getPlayerTeam(player.getUniqueId()));
					data.setDemandeur(player);
					data.setD??fenceurs(TName1);
					AssautRequestGUI.assautRequestGUIMain(player);
				}
				break;
			case BOOK:
				if (current.getItemMeta().getDisplayName().contains("??brejoindre la team ")) {
					String TName11 = current.getItemMeta().getDisplayName();
					TName11 = TName11.replace("??brejoindre la team ", "");
					player.performCommand("team join " + TName11);
				} else if (current.getItemMeta().getDisplayName().contains("??bpostuler pour rejoindre la team ")) {
					String TName11 = current.getItemMeta().getDisplayName();
					TName11 = TName11.replace("??bpostuler pour rejoindre la team ", "");
					player.performCommand("team join " + TName11);
				} else if (current.getItemMeta().getDisplayName().contains("??cLa team est priv??e")) {
					player.sendMessage(teams.prefix
							+ " ??cCette team n'accepte pas des demandes pour rejoindre. C'est elle qui doit vous inviter.");
				}
				return;
			case CHEST:
				String TName11 = current.getItemMeta().getDisplayName();
				TName11 = TName11.replace("??bRichesses de la team ", "");

				Location location = TeamData.getBankChest(TName11);
				if (location == null) {
					player.sendMessage(
							teams.prefix + " ??cLa team ??c??l" + TName11 + " ??cne poss??de pas de coffre de team.");
					return;
				}

				Block block = location.getBlock();
				if (block.getType() != Material.CHEST) {
					player.sendMessage(teams.prefix + " ??cLa team ??c??l" + TName11
							+ " ??cne poss??de pas de coffre de team (ou il a un probl??me).");
					return;
				}
				Chest chest = (Chest) block.getState();
				Inventory inve = chest.getInventory();
				Inventory inv = Bukkit.createInventory(null, 36, "??9Richesses de la team ??b" + TName11);
				for (int i = 0; i < 27; i++) {
					inv.setItem(i, inve.getItem(i));
				}
				inv.setItem(31, GUIInterface.newItemS(Material.REDSTONE, "??cRetour au menu pr??c??dent"));
				player.openInventory(inv);
			default:
				break;
			}
			break;

		case "??9Demande d'assi??gement":
			event.setCancelled(true);
			switch (current.getType()) {
			case DIAMOND_SWORD:
				player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
				break;
			case CLOCK:
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
				AssautRequestGUI.assautRequestGUIPhasesMain(player);
				break;
			case IRON_DOOR:
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
				AssautRequestGUI.assautRequestGUIAccess(player);
				break;
			case PAPER:
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				break;
			case RED_CONCRETE:
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
				AssautRequestData data = AssautRequestData.AssautRequestData.get(player.getName());
				data.setAttaquants(null);
				data.setDemandeur(null);
				data.setD??fenceurs(null);
				data.setAcc??s("open");
				data.setTpsPr??paration(50);
				data.setTpsAttaque(120);
				player.closeInventory();
				break;
			case GREEN_CONCRETE:
				String rank = TeamData.getPlayerRank(player.getUniqueId());
				if (rank.equals("membres-de-confiance") || rank.equals("sous-chefs") || rank.equals("chefs")) {
					try {
						AssautRequestData data11 = AssautRequestData.AssautRequestData.get(player.getName());
						AssautRequestSendData data1 = AssautRequestSendData.AssautRequestSendData
								.get(data11.getD??fenceurs());
						player.sendMessage(teams.prefix
								+ " ??cCette team a d??j?? re??u une demande d'assaut de la part de la team ??c??l"
								+ data1.getAttaquants() + "??c.");
					} catch (NullPointerException e) {
						player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
						AssautRequestData data1 = AssautRequestData.AssautRequestData.get(player.getName());
						if (!data1.getAttaquants().equals(data1.getD??fenceurs())) {
							TeamData.attackRequest(data1.getAttaquants(), data1.getD??fenceurs(), data1.getDemandeur(),
									data1.getAcc??s(), data1.getTpsPr??paration(), data1.getTpsAttaque());
						} else {
							player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
							player.sendMessage(teams.prefix + " ??cVous ne pouvez pas vous assi??ger vous-m??me !");
						}
						player.closeInventory();
					}
				} else {
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					player.sendMessage(teams.prefix
							+ " ??cVous n'avez pas la permission requise pour envoyer une demande d'assaut !");
				}
				player.closeInventory();
				break;
			default:
				break;
			}
			break;

		case "??9Configuration de l'acc??s":
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			switch (current.getType()) {
			case IRON_DOOR:
				AssautRequestData data = AssautRequestData.AssautRequestData.get(player.getName());
				data.setAcc??s("close");
				AssautRequestGUI.assautRequestGUIMain(player);
				break;
			case ACACIA_DOOR:
				AssautRequestData data1 = AssautRequestData.AssautRequestData.get(player.getName());
				data1.setAcc??s("open");
				AssautRequestGUI.assautRequestGUIMain(player);
				break;
			case REDSTONE:
				AssautRequestGUI.assautRequestGUIMain(player);
				break;
			default:
				break;
			}
			break;

		case "??9Configuration des phases":
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			switch (current.getType()) {
			case REPEATER:
				AssautRequestGUI.assautRequestGUIPhasesPr??parationTPS(player);
				break;
			case TNT:
				AssautRequestGUI.assautRequestGUIPhasesAttaqueTPS(player);
				break;
			case STONE_SWORD:
				player.getWorld().strikeLightning(player.getLocation());
				player.sendMessage(teams.prefix + " ??9Je t'avais pr??venu...");
				break;
			case REDSTONE:
				AssautRequestGUI.assautRequestGUIMain(player);
				break;
			default:
				break;
			}
			break;

		case "??9Configuration du temps de pr??paration":
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			switch (current.getType()) {
			case REDSTONE:
				AssautRequestGUI.assautRequestGUIPhasesMain(player);
				break;
			case PLAYER_HEAD:
				if (current.getItemMeta().getDisplayName().equals("??b-10 minutes")) {
					AssautRequestData data = AssautRequestData.AssautRequestData.get(player.getName());
					if (data.getTpsPr??paration() == 10) {
						player.sendMessage(teams.prefix
								+ " ??cVous ne pouvez pas d??finir le temps de la phase de pr??paration en dessous de 10 minutes.");
					} else {
						data.setTpsPr??paration(data.getTpsPr??paration() - 10);
						AssautRequestGUI.assautRequestGUIPhasesPr??parationTPS(player);
					}
				} else if (current.getItemMeta().getDisplayName().equals("??b+10 minutes")) {
					AssautRequestData data = AssautRequestData.AssautRequestData.get(player.getName());
					if (data.getTpsPr??paration() == 90) {
						player.sendMessage(teams.prefix
								+ " ??cVous ne pouvez pas d??finir le temps de la phase de pr??paration au dessus de 90 minutes.");
					} else {
						data.setTpsPr??paration(data.getTpsPr??paration() + 10);
						AssautRequestGUI.assautRequestGUIPhasesPr??parationTPS(player);
					}
				}
			default:
				break;
			}
			break;

		case "??9Configuration du temps d'attaque":
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			switch (current.getType()) {
			case REDSTONE:
				AssautRequestGUI.assautRequestGUIPhasesMain(player);
				break;
			case PLAYER_HEAD:
				if (current.getItemMeta().getDisplayName().equals("??b-10 minutes")) {
					AssautRequestData data = AssautRequestData.AssautRequestData.get(player.getName());
					if (data.getTpsAttaque() == 30) {
						player.sendMessage(teams.prefix
								+ " ??cVous ne pouvez pas d??finir le temps de la phase d'attaque en dessous de 30 minutes.");
					} else {
						data.setTpsAttaque(data.getTpsAttaque() - 10);
						AssautRequestGUI.assautRequestGUIPhasesAttaqueTPS(player);
					}
				} else if (current.getItemMeta().getDisplayName().equals("??b+10 minutes")) {
					AssautRequestData data = AssautRequestData.AssautRequestData.get(player.getName());
					if (data.getTpsAttaque() == 240) {
						player.sendMessage(teams.prefix
								+ " ??cVous ne pouvez pas d??finir le temps de la phase d'attaque au dessus de 240 minutes.");
					} else {
						data.setTpsAttaque(data.getTpsAttaque() + 10);
						AssautRequestGUI.assautRequestGUIPhasesAttaqueTPS(player);
					}
				}
			default:
				break;
			}
			break;

		case "??9Conditions de l'assaut":
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			switch (current.getType()) {
			case RED_CONCRETE:
				try {
					AssautRequestSendData data = AssautRequestSendData.AssautRequestSendData
							.get(TeamData.getPlayerTeam(player.getUniqueId()));
					player.performCommand("team denyAttack " + data.getAttaquants());
				} catch (NullPointerException e) {
					player.sendMessage("??eSimplebot ??8?? ??cVous n'avez aucune demande d'assaut en cours.");
					player.closeInventory();
				}
				break;
			case GREEN_CONCRETE:
				try {
					AssautRequestSendData data1 = AssautRequestSendData.AssautRequestSendData
							.get(TeamData.getPlayerTeam(player.getUniqueId()));
					if (ClaimData.getFirstLoc(data1.getD??fenceurs()) == null) {

						player.sendMessage(teams.prefix + " ??cCette team ne poss??de pas de rep??re claim.");
						player.performCommand("team denyAttack " + data1.getAttaquants());
						return;
					}
					if (TeamData.getBankChest(data1.getD??fenceurs()) == null) {

						player.sendMessage(teams.prefix + " ??cCette team ne poss??de pas de coffre banque de team.");
						player.performCommand("team denyAttack " + data1.getAttaquants());
						return;
					}
					player.closeInventory();
					Assaut assaut1 = new Assaut();
					assaut1.startAssaut(data1, teams);
					break;
				} catch (NullPointerException e) {
					player.sendMessage(
							"??eSimplebot ??8?? ??cVous n'avez aucune demande d'assaut en cours, ou la team n'a pas de claim ou de coffre banque.");
					player.closeInventory();
				}
				break;
			default:
				break;
			}
			break;

		default:
			break;

		}

		if (event.getView().getTitle().contains("??9Richesses de la team ??b")) {
			event.setCancelled(true);
			if (current.getType() == Material.REDSTONE) {
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
				String name = event.getView().getTitle();
				name = name.replace("??9Richesses de la team ??b", "");
				TeamCommandInfoGUI.teamInfoGUIMain(player, name);
				return;
			}
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			return;
		}

	}

}

/*
 * case "": event.setCancelled(true);
 * player.playSound(player.getLocation(),Sound.UI_BUTTON_CLICK, 10, 10); switch
 * (current.getType()) { case ITEM: break; default: break; } break;
 */
