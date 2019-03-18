/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

//import View.DashboardView;
//import View.View;
import Mp3agic.InvalidDataException;
import Mp3agic.NotSupportedException;
import Mp3agic.UnsupportedTagException;
import View.View;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class FacadeModel{
    private AccountInterface user;
    private ObservableList<SongInterface> songs;
    private ObservableList<PlaylistInterface> groups;
    private SongInterface currentSong, selectedSong;

    private Service accountService;
    private Service playlistService;
    private Service songService;
    private AudioParserInterface parser;
    private PlaylistInterface queue;
    private ObservableList<View> view;

    public FacadeModel() {
        songs = FXCollections.observableArrayList();
        groups = FXCollections.observableArrayList();
        view = FXCollections.observableArrayList();
        accountService = new AccountService();
        playlistService = new PlaylistService();
        songService = new SongService();
        parser = new AudioParser();
        queue = new Queue();
    }

    public void attach(View view) {
        this.view.add(view);
    }

    public void update() {
        for(View v: view) {
            v.update();
        }
    }

    public SongInterface getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(SongInterface currentSong) {
        this.currentSong = currentSong;
        update();
    }
    
    public AccountInterface getUser() {
        return user;
    }

    public void setUser(AccountInterface user) {
        this.user = user;
        update();
    }

    public ObservableList<SongInterface> getSongs() {
        return songs;
    }

    public void setSongs(ObservableList<SongInterface> songs) {
        this.songs = songs;
        update();
    }

    public ObservableList<PlaylistInterface> getGroups() {
        return groups;
    }

    public void setGroups(ObservableList<PlaylistInterface> groups) {
        this.groups = groups;
        update();
    }

    //METHOD CONNECTED TO DATABASE
    /*Pass the username and password of the login attempt
    * Compares the username and password if it matches anthing in the database
    * */
    public boolean login(String username, String password) throws SQLException {
        ObservableList<Object> accounts = null;
        accounts = accountService.getAll();
        if (accounts != null){
            for (Object temp : accounts) {
                if (((Account)temp).getUsername().compareTo(username) == 0 && ((Account)temp).getPassword().compareTo(password) == 0){
                    user = (AccountInterface) temp;
                    user.setPlaylists(getUserPlaylist());
                    user.setSongs(getUserSongs());
                    update();
                    return true;
                }
            }
        }
        return false;
    }

    public void logout(){
        user = null;
        update();
    }

    public void addSongLocally(String filelocation){
        if(songs == null)
            songs = FXCollections.observableArrayList();
        songs.add(CreateSongFromLocal.CreateSong(filelocation));
        update();
    }

    public void removeSongLocally(SongInterface song) {
        System.out.println(songs == null);
        if(songs != null) {
            songs.remove(song);
            System.out.println("song removed");
        }
        update();
    }
    
    /*Add/import one song to the database under the current user
    * */
    public boolean addSong(String filelocation) throws SQLException {
        ObservableList<Object> songs = null;
        songs = songService.getAll();
        SongInterface s = CreateSongFromLocal.CreateSong(filelocation);
        s.setUser(this.user.getUsername());

        if (songs.size() == 0) {
            s.setSongid("S01");
            if(songService.add(s)){
                user.setSongs(getUserSongs());
                return true;
            }
        } else {
            for (Object temp : songs) {
                if (((SongInterface) temp).getName().compareToIgnoreCase(s.getName()) != 0
                        && ((SongInterface) temp).getArtist().compareToIgnoreCase(s.getArtist()) != 0
                        && ((SongInterface) temp).getAlbum().compareToIgnoreCase(s.getAlbum()) != 0) {
                    s.setSongid(String.format("S%02d", songs.size() + 1));
                    if (songService.add(s)) {
                        user.setSongs(getUserSongs());
                        update();
                        return true;
                    }
                }else{
                    boolean b = ((SongService)songService).addSongtoUser(((SongInterface) temp).getSongid(), user.getUsername());
                    System.out.println(b);
                    user.setSongs(getUserSongs());
                    update();
                    return b;
                }
            }
        }
        update();
        return false;
    }


    public SongInterface getSong(String songid) throws SQLException {
        return ((SongService)songService).getSong(songid, user.getUsername());
    }
    /*Deletes one specific song in the database using songid
    * Automatically deletes the song in the playlist that contains the song*/
    public boolean deleteSong(String songid) throws SQLException {
        boolean b =((SongService)songService).delete(songid, (AccountInterface) user);
        user.setSongs(getUserSongs());
        update();
        return b;
    }

    /*Updates the title of the song given the new name and the song that wants to be changed*/
    public boolean updateSongName(String name, SongInterface s) throws SQLException, UnsupportedTagException, NotSupportedException, InvalidDataException, IOException {
        SongInterface old = s;
        s.setName(name);
        parser.editSongDetails(old, s);
        boolean b = ((SongService)songService).update(s.getSongid(), s, s.getUser());
        user.setSongs(getUserSongs());
        update();
        return b;
    }

    /*Updates the genre of the song given the new name and the song that wants to be changed*/
    public boolean updateSongGenre(String genre, SongInterface s) throws SQLException, UnsupportedTagException, NotSupportedException, InvalidDataException, IOException {
        SongInterface old = s;
        s.setGenre(genre);
        parser.editSongDetails(old, s);
        boolean b = ((SongService)songService).update(s.getSongid(), s, s.getUser());
        user.setSongs(getUserSongs());
        update();
        return b;
    }

    /*Updates the artist of the song given the new name and the song that wants to be changed*/
    public boolean updateSongArtist(String artist, SongInterface s) throws SQLException, UnsupportedTagException, NotSupportedException, InvalidDataException, IOException {
        SongInterface old = s;
        s.setArtist(artist);
        parser.editSongDetails(old, s);
        boolean b = ((SongService)songService).update(s.getSongid(), s, s.getUser());
        user.setSongs(getUserSongs());
        update();
        return b;
    }

    /*Updates the album of the song given the new name and the song that wants to be changed*/
    public boolean updateSongAlbum(String album, SongInterface s) throws SQLException, UnsupportedTagException, NotSupportedException, InvalidDataException, IOException {
        SongInterface old = s;
        s.setAlbum(album);
        parser.editSongDetails(old, s);
        boolean b = ((SongService)songService).update(s.getSongid(), s, s.getUser());
        user.setSongs(getUserSongs());
        update();
        return b;
    }

    /*Updates the year of the song given the new name and the song that wants to be changed*/
    public boolean updateSongYear(int year, SongInterface s) throws SQLException, UnsupportedTagException, NotSupportedException, InvalidDataException, IOException {
        SongInterface old = s;
        s.setYear(year);
        parser.editSongDetails(old, s);
        boolean b = ((SongService)songService).update(s.getSongid(), s, s.getUser());
        user.setSongs(getUserSongs());
        update();
        return b;
    }

    /*Updates the track number of the song given the new name and the song that wants to be changed*/
    public boolean updateSongTrackNumber(int trackNumber, SongInterface s) throws SQLException, UnsupportedTagException, NotSupportedException, InvalidDataException, IOException {
        SongInterface old = s;
        s.setTrackNumber(trackNumber);
        parser.editSongDetails(old, s);
        boolean b = ((SongService)songService).update(s.getSongid(), s, s.getUser());
        user.setSongs(getUserSongs());
        update();
        return b;
    }

    /*Updates the albumart of the song given the new name and the song that wants to be changed*/
    public boolean updateSongCover(File img, SongInterface s) throws SQLException, UnsupportedTagException, NotSupportedException, InvalidDataException, IOException {
        SongInterface old = s;
        s = parser.setSongImage(old, img);
        parser.editSongDetails(old, s);
        boolean b = ((SongService)songService).update(s.getSongid(), s, s.getUser());
        user.setSongs(getUserSongs());
        update();
        return b;
    }

    /*Updates the filename of the song given the new name and the song that wants to be changed*/
    public boolean updateSongFileName(String filename, SongInterface s) throws SQLException {
        s.setFilename(filename);
        boolean b = ((SongService)songService).update(s.getSongid(), s, s.getUser());
        user.setSongs(getUserSongs());
        update();
        return b;
    }

    /*Updates the location the song will be save given the new name and the song that wants to be changed*/
    public boolean updateSongFileLocation(String filelocation, SongInterface s) throws SQLException {
        s.setFilename(filelocation);
        boolean b = ((SongService)songService).update(s.getSongid(), s, s.getUser());
        user.setSongs(getUserSongs());
        update();
        return b;
    }

    public File getsongImage(SongInterface s) throws InvalidDataException, IOException, UnsupportedTagException, NotSupportedException {
        return parser.getSongImage(s);
    }


    /*Updates the album of the song given the new name and the song that wants to be changed*/
    public boolean playSong(SongInterface s) throws SQLException{
        s.setTimesplayed(s.getTimesplayed() + 1);
        setCurrentSong(s);
        boolean b = ((SongService)songService).update(s.getSongid(), s, s.getUser());
        user.setSongs(getUserSongs());
        update();
        return b;
    }

    /*Returns all the songs in the database*/
    public ObservableList<SongInterface> getAllSong() throws SQLException {
        ObservableList<Object> o = songService.getAll();
        ObservableList<SongInterface> songs = FXCollections.observableArrayList();
        if(o != null) {
            for (Object temp : o) {
                songs.add((SongInterface)temp);
            }
            return songs;
        }
        return null;
    }

    /*Returns the songs that the user imported*/
    public ObservableList<SongInterface> getUserSongs() throws SQLException {
        ObservableList<SongInterface> songs  = ((SongService)songService).getUserSong(user.getUsername());
        if(songs != null) {
            return songs;
        }
        return null;
    }

    /*Adds the playlist in the database*/
    public boolean addPlaylist(PlaylistInterface p) throws SQLException {
        ObservableList<Object> playlists = null;
        playlists = playlistService.getAll();

        if (playlists == null) {
            p.setPlaylistid("P01");
            if(((PlaylistService)playlistService).add(p, (Account)user)){
                user.setPlaylists(getUserPlaylist());
                update();
                return true;
            }
        } else {
            p.setPlaylistid(String.format("P%02d", playlists.size() + 1));
            if(((PlaylistService)playlistService).add(p, (Account)user)){
                user.setPlaylists(getUserPlaylist());
                update();
                return true;
            }
        }
        update();
        return false;
    }

    /*Adds one song to the playlist*/
    public boolean addSongToPlaylist(SongInterface s, PlaylistInterface p) throws SQLException {
        PlaylistInterface playlist = ((PlaylistService)playlistService).getPlaylist(p.getPlaylistid(), user.getUsername());
        ObservableList<SongInterface> songs = playlist.getSongs();
        for(SongInterface temp: songs){
            if(temp.getSongid().equals(s.getSongid()))
                return false;
        }
        boolean b = ((PlaylistService)playlistService).addSongPlaylist(s, p.getPlaylistid());
        user.setPlaylists(getUserPlaylist());
        update();
        return b;
    }

    /*Deletes one song in the playlist*/
    public boolean deleteSongToPlaylist(SongInterface s, PlaylistInterface p) throws SQLException {
        boolean b = ((PlaylistService)playlistService).deleteSongInPlaylist(s.getSongid(), p.getPlaylistid());
        user.setPlaylists(getUserPlaylist());
        update();
        return b;
    }

    /*Takes specifc playlist*/
    public PlaylistInterface getPlaylist(String playlistid) throws SQLException {
        return ((PlaylistService)playlistService).getPlaylist(playlistid, user.getUsername());
    }


    /*Takes all the playlist the user created*/
    public ObservableList<PlaylistInterface> getUserPlaylist() throws SQLException {
        PlaylistFactory playlistFactory = new AlbumPlaylistConcreteFactory();
        ObservableList<PlaylistInterface> playlists = playlistFactory.playlistFactoryMethod(user.getUsername());
        if(playlists != null) {
            setGroups(playlists);
            return playlists;
        }
        return null;
    }

    /*Returns list of playlist grouped by album*/
    public ObservableList<PlaylistInterface> getAlbumPlaylist() throws SQLException {
        PlaylistFactory playlistFactory = new AlbumPlaylistConcreteFactory();
        return playlistFactory.playlistFactoryMethod(user.getUsername());
    }

    /*Returns list of playlist grouped by artist*/
    public ObservableList<PlaylistInterface> getArtistPlaylist() throws SQLException {
        PlaylistFactory playlistFactory = new ArtistPlaylistConcreteFactory();
        return playlistFactory.playlistFactoryMethod(user.getUsername());
    }

    /*Returns list of playlist grouped by year*/
    public ObservableList<PlaylistInterface> getYearPlaylist() throws SQLException {
        PlaylistFactory playlistFactory = new YearPlaylistConcreteFactory();
        return playlistFactory.playlistFactoryMethod(user.getUsername());
    }

    /*Returns list of playlist grouped by genre*/
    public ObservableList<PlaylistInterface> getGenrePlaylist() throws SQLException {
        PlaylistFactory playlistFactory = new GenrePlaylistConcreteFactory();
        return playlistFactory.playlistFactoryMethod(user.getUsername());
    }

    /*Deletes the playlist in the database*/
    public boolean deletePlaylist(String playlistid) throws SQLException {
        boolean b = playlistService.delete(playlistid);
        user.setPlaylists(getUserPlaylist());
        update();
        return b;
    }

    /*Edits the name of the playlist*/
    public boolean renamePlaylist(String playlistname, PlaylistInterface p) throws SQLException {
        p.setName(playlistname);
        boolean b = playlistService.update(p.getPlaylistid(), p);
        user.setPlaylists(getUserPlaylist());
        update();
        return b;
    }

    /*Edits the name of the user*/
    public boolean updateNameofUser(String name, AccountInterface a) throws SQLException {
        a.setName(name);
        boolean b = accountService.update(a.getUsername(), a);
        update();
        return b;
    }

    /*Edits the password of the user*/
    public boolean updateUserPassword(String password, AccountInterface a) throws SQLException {
        a.setPassword(password);
        boolean b = accountService.update(a.getUsername(), a);
        update();
        return b;
    }

    /*Register/sign-up a new user and saves it to the database
    * Needs to pass account data type with*/
    public boolean createUser(Account a) throws SQLException {
        ObservableList<Object> accounts = null;
        accounts = accountService.getAll();

        if (accounts == null) {
            if(accountService.add(a)){
                user = (AccountInterface) a;
                return true;
            }

        } else {
            for (Object temp : accounts) {
                if (((Account)temp).getUsername().compareTo(a.getUsername()) == 0)
                    return false;
            }
            if(accountService.add(a)){
                user = (AccountInterface) a;
                return true;
            }
        }
        return true;
    }

    public PlaylistInterface getQueue() {
        return queue;
    }

    public void setQueue(PlaylistInterface queue) {
        this.queue = queue;
    }

    //METHOD CONNECTED TO DATABASE
    
    public FacadeModel getState() {
        return this;
    }

    public SongInterface getSelectedSong() {
        return selectedSong;
    }

    public void setSelectedSong(SongInterface selectedSong) {
        this.selectedSong = selectedSong;
    }

}
