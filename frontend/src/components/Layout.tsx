import { AppBar, Toolbar, Typography, Box, Container, Button } from '@mui/material';
import { Outlet } from 'react-router-dom';
import { ThemeToggle } from './ThemeToggle';
import { useAuth } from '../contexts/AuthContext';

export default function Layout() {
  const { isAuthenticated, login, logout, getUser } = useAuth();
  const user = getUser();

  if (!isAuthenticated) {
    return (
      <Box sx={{ 
        display: 'flex', 
        flexDirection: 'column', 
        minHeight: '100vh',
        alignItems: 'center',
        justifyContent: 'center'
      }}>
        <Typography variant="h4" gutterBottom>
          Member Management
        </Typography>
        <Typography variant="body1" sx={{ mb: 3 }}>
          Please log in to access the application
        </Typography>
        <Button variant="contained" onClick={login}>
          Login
        </Button>
      </Box>
    );
  }

  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Member Management
          </Typography>
          {user && (
            <Typography variant="body2" sx={{ mr: 2 }}>
              Welcome, {user.preferred_username || user.name}
            </Typography>
          )}
          <Button color="inherit" onClick={logout} sx={{ mr: 1 }}>
            Logout
          </Button>
          <ThemeToggle />
        </Toolbar>
      </AppBar>
      <Box display="flex"
        justifyContent="center"
        alignItems="center"
        width="100%"
        sx={{ 
            width: '100%',
            // maxWidth: '1200px',
            px: { xs: 2, sm: 3 },
            py: { xs: 2, sm: 3 }
          }}>
            <Outlet />
      </Box>
    </>
  );
}
