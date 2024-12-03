import { useFileContext } from './FileContext';
import {
    Container,
    Typography,
    Button,
    List,
    ListItem,
    ListItemText,
    Box,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
} from '@mui/material';

const AllMovies = () => {
    const { message, getMessage, files, todos, getTodos, deleteFile, fileToDelete, setFileToDelete } = useFileContext();

    return (
        <Container maxWidth="md" component="main" sx={{ padding: '2rem 0' }}>
            <div style={{ border: '1px solid gray', borderRadius: '15px', marginBottom: '20px' }}>
                <Typography variant="body1" sx={{ textAlign: 'center', marginBottom: '2rem' }}>
                    List of files:
                </Typography>
                <List>
                    {files && files.length > 0 ? (
                        files.map((file, index) => (
                            <ListItem key={index} sx={{ justifyContent: 'space-between', width: '100%' }}>
                                <ListItemText primary={file.fileOriginalName || `File ${index + 1}`} />
                                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                    <img src={'pdficon.png'} alt="PDF Icon" style={{ width: '24px', height: '24px' }} />
                                    <Button
                                        variant="outlined"
                                        color="primary"
                                        onClick={() => {setFileToDelete(file)}}
                                        sx={{ fontSize: '10px', padding: '4px', margin: '0px', marginLeft: '10px' }}
                                    >
                                        Remove
                                    </Button>
                                </Box>
                            </ListItem>
                        ))
                    ) : (
                        <Typography variant="body1" sx={{ textAlign: 'center', marginBottom: '2rem' }}>
                            {files ? "List is empty" : "Loading..."}
                        </Typography>
                    )}
                </List>
            </div>
            <div style={{ border: '1px solid gray', borderRadius: '15px' }}>
                <Typography variant="body1" sx={{ textAlign: 'center', marginBottom: '2rem' }}>
                    Older:
                </Typography>
                <Button
                    variant="contained"
                    color="primary"
                    onClick={getMessage}
                    sx={{ display: 'block', margin: '0 auto', marginBottom: '2rem' }}
                >
                    Main message
                </Button>
                <Button
                    variant="contained"
                    color="primary"
                    onClick={getTodos}
                    sx={{ display: 'block', margin: '0 auto', marginBottom: '2rem' }}
                >
                    Get todos!
                </Button>

                <Typography variant="body1" sx={{ textAlign: 'center', marginBottom: '2rem' }}>
                    {message}
                </Typography>
                <Typography variant="body1" sx={{ textAlign: 'center', marginBottom: '2rem' }}>
                    {JSON.stringify(todos)}
                </Typography>
            </div>
            <Dialog 
                open={fileToDelete ? true : false} 
                onClose={() => {setFileToDelete(null)}}
                sx={{
                    '& .MuiDialog-paper': {
                    padding: 2,
                    borderRadius: 2,
                    backgroundColor: '#f5f5f5',
                    }
                }}
                >
                <DialogTitle sx={{ fontWeight: 'bold', color: '#1976d2' }}>Confirm Deletion</DialogTitle>
                <DialogContent>
                    <Typography sx={{ fontSize: '1rem', color: '#333' }}>
                    Are you sure you want to delete?
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button 
                    onClick={() => {setFileToDelete(null)}}
                    color="primary" 
                    sx={{
                        textTransform: 'none',
                        color: '#1976d2',
                        '&:hover': {
                        backgroundColor: '#e3f2fd',
                        }
                    }}
                    >
                    Cancel
                    </Button>
                    <Button 
                    onClick={() => {deleteFile(fileToDelete)}}
                    color="primary" 
                    variant="contained" 
                    sx={{
                        textTransform: 'none',
                        backgroundColor: '#1976d2',
                        '&:hover': {
                        backgroundColor: '#1565c0',
                        }
                    }}
                    >
                    Confirm
                    </Button>
                </DialogActions>
                </Dialog>
        </Container>
    );
};

export default AllMovies;
