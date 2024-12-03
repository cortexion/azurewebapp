import { SnackbarCloseReason } from '@mui/material';
import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';

type FileContextType = {
    snackbarOpen: boolean;
    fileToDelete: any;
    setFileToDelete: (file: any) => void;
    handleSnackbarClose: (event: React.SyntheticEvent | Event, reason?: SnackbarCloseReason) => void
    uploadFiles: (body: any) => Promise<void>;
    setFiles: React.Dispatch<React.SetStateAction<null | any[]>>;
    deleteFile: (fileUuid: string) => Promise<void>;
    message: string;
    getMessage: () => Promise<void>;
    todos: any[];
    getTodos: () => Promise<void>;
    files: null | any[];
    getFiles: () => Promise<void>;
};

export const FileContext = createContext<FileContextType>({
    snackbarOpen: false,
    fileToDelete: null,
    setFileToDelete: () => {},
    handleSnackbarClose: async (event: React.SyntheticEvent | Event, reason?: SnackbarCloseReason) => {},
    uploadFiles: async (body: any) => {},
    setFiles: () => {},
    deleteFile: async (fileUuid: string) => {},
    message: '',
    getMessage: async () => {},
    todos: [],
    getTodos: async () => {},
    files: [],
    getFiles: async () => {},
});

// Hook to access context
export const useFileContext = () => useContext(FileContext);

export const FileContextProvider = ({ children }: { children: React.ReactNode }) => {
    const [files, setFiles] = useState<null | any[]>(null);
    const [message, setMessage] = useState<string>('');
    const [todos, setTodos] = useState<any[]>([]);
    const [snackbarOpen, setSnackbarOpen] = useState<boolean>(false);
    const [fileToDelete, setFileToDelete] = useState<any>(null);

    const uploadFiles = useCallback(async (data: any) => {        
        try {
            const response = await fetch('/api/files/upload', {
              method: 'POST',
              body: data,
            });
      
            if (response.ok) {
                const responseData = await response.json(); // Parse the response as JSON
                const filesToSet = responseData.map((file: any) => ({
                    id: file.id,
                    fileOriginalName: file.fileOriginalName,
                }));

            setFiles(files => [...files || [], ...filesToSet]);
            setSnackbarOpen(true);
            } else {
                console.error('Failed to upload files');
            }
            } catch (error) {
                console.error('Error uploading files:', error);
            }
    }, []);

    const getFiles = useCallback(async () => {
        try {
            const response = await fetch(`/api/files`, {
                method: 'GET',
            });
            if (response.status === 200) {
                const todosJson = await response.json();
                setFiles(todosJson);
            } else {
                console.error('Error! Please retry...');
            }
        } catch (e) {
            console.log('Error: ', e);
            throw new Error((e as Error).message);
        }
    }, []);

    const deleteFile = useCallback(async (fileToDelete: any) => {
        try {
            const response = await fetch(`/api/files/${fileToDelete.id}`, {
              method: 'DELETE'
            });
      
            if (response.ok) {
                const responseData = await response.json();
                console.log(responseData.message);
                const currentFiles = Array.isArray(files) ? files.filter((file: any) => file.id !== fileToDelete.id) : [];
                setFiles(currentFiles);
                setFileToDelete(null);
            } else {
                console.error('Failed to delete');
            }
            } catch (error) {
                console.error('Error deleting file:', error);
            }
    }, [files]);

    const getMessage = useCallback(async () => {
        try {
            const response = await fetch(`/api/todos/message`, {
                method: 'GET',
            });
            if (response.status === 200) {
                const messageJson = await response.json();
                setMessage(messageJson.message);
            } else {
                console.error('Error! Please retry...');
            }
        } catch (e) {
            console.log('Error: ', e);
            throw new Error((e as Error).message);
        }
    }, []);

    const getTodos = useCallback(async () => {
        try {
            const response = await fetch(`/api/todos`, {
                method: 'GET',
            });
            if (response.status === 200) {
                const todosJson = await response.json();
                setTodos(todosJson.todos);
            } else {
                console.error('Error! Please retry...');
            }
        } catch (e) {
            console.log('Error: ', e);
            throw new Error((e as Error).message);
        }
    }, []);

    const handleSnackbarClose = (
        event: React.SyntheticEvent | Event,
        reason?: SnackbarCloseReason,
      ) => {
        if (reason === 'clickaway') {
          return;
        }    
        setSnackbarOpen(false);
      };

    useEffect(() => {
        if (!files) {
            getFiles();
        }
        //console.log('Files:', files);
    }, [files, getFiles]);

    return (
        <FileContext.Provider value={{ snackbarOpen, fileToDelete, setFileToDelete, handleSnackbarClose, uploadFiles, deleteFile, setFiles, getMessage, message, getFiles, files, getTodos, todos }}>
            {children}
        </FileContext.Provider>
    );
};