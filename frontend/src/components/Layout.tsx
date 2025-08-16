import { AppBar, Toolbar, Typography, Box, Container } from '@mui/material';
import { Outlet } from 'react-router-dom';

export default function Layout() {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div">
            Member Management
          </Typography>
        </Toolbar>
      </AppBar>
      <Box sx={{ 
        flex: 1, 
        display: 'flex', 
        flexDirection: 'column',
        width: '100%',
      }}>
        <Container 
          maxWidth={false} 
          sx={{ 
            flex: 1,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            maxWidth: '1200px !important',
            px: { xs: 2, sm: 3 },
            py: { xs: 2, sm: 3 }
          }}
        >
          <Box sx={{ width: '100%' }}>
            <Outlet />
          </Box>
        </Container>
      </Box>
    </Box>
  );
}
