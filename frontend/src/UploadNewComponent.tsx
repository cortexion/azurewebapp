import { useState } from "react";
import { useFileContext } from "./FileContext";
import { Box, Button, SnackbarContent, Typography } from '@mui/material';
import Snackbar from '@mui/material/Snackbar';

const UploadNewComponent = () => {
    const { uploadFiles, snackbarOpen, handleSnackbarClose } = useFileContext();

    const [formData, setFormData] = useState({
        files: [] as File[],
    });

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) {
            setFormData({ ...formData, files: Array.from(e.target.files) }); // Convert FileList to Array
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        const data = new FormData();
        formData.files.forEach((file) => {
            data.append('files', file); // Use the same key for all files
        });
        uploadFiles(data);
        setFormData({ ...formData, files: [] });
    };

    const removeFile = (fileName: string) => {
        setFormData((prevState) => ({
            ...prevState,
            files: prevState.files.filter(file => file.name !== fileName), // Filter out the file to be removed
        }));
    };

    return (
        <Box
            display="flex"
            flexDirection="column" // Stack elements vertically
            alignItems="center"
            sx={{ mt: 4 }}
        >
            <form onSubmit={handleSubmit}>
                <Box sx={{ maxWidth: '360px', minWidth: '360px', boxShadow: 3, padding: '20px', borderRadius: '10px' }}>
                <Box display="flex" alignItems="center">
                    <input
                        type="file"
                        name="files"
                        multiple
                        onChange={handleFileChange}
                        style={{ display: 'none' }} // Hide the default file input
                        id="file-upload"
                    />
                    <label htmlFor="file-upload">
                        <Button style={{minWidth: '140px'}} variant="contained" component="span">
                            Select Files
                        </Button>
                    </label>
                    <div style={{minHeight: '100px', maxHeight: '100px', overflow: 'auto' }}>
                    {formData.files.length > 0 && (
                        <div>
                            <ul>
                                {formData.files.map((file, index) => (
                                    <li key={index} style={{ display: 'flex', alignItems: 'center' }}>
                                        <Typography variant="body1" component="span" sx={{ mr: 1 }}>{file.name}</Typography>
                                        <Button 
                                            size="small" 
                                            color="secondary" 
                                            onClick={() => removeFile(file.name)} 
                                            sx={{ minWidth: 'auto', padding: 0 }}
                                        >
                                            [X]
                                        </Button>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}</div>
                </Box>
                <Box mb={2} mt={2}>
                    <Button type="submit" variant="contained" color="primary">
                        Submit
                    </Button>
                </Box>
                </Box>
            </form>
            <Snackbar
            anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
            open={snackbarOpen}
            autoHideDuration={2500}
            onClose={handleSnackbarClose}
        >
            <SnackbarContent
                style={{
                    backgroundColor: 'orange',
                    color: 'white',
                    padding: '5px',
                    paddingLeft: '15px',
                    borderRadius: '5px',
                    fontSize: '15px',
                }}
                message="File(s) uploaded successfully."
            />
        </Snackbar>
        </Box>
    );
};

export default UploadNewComponent;