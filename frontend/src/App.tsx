import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Box, CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import { NotificationProvider } from './contexts/NotificationContext';
import { LoadingProvider } from './contexts/LoadingContext';
import MemberList from './components/MemberList';
import MemberDetail from './components/MemberDetail';
import MemberForm from './components/MemberForm';
import Layout from './components/Layout';

const theme = createTheme();

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <NotificationProvider>
        <LoadingProvider>
          <Router>
            <Box sx={{ 
              minHeight: '100vh',
              display: 'flex',
              flexDirection: 'column',
              width: '100%'
            }}>
              <Routes>
                <Route element={<Layout />}>
                  <Route path="/members" element={<MemberList />} />
                  <Route path="/members/new" element={<MemberForm />} />
                  <Route path="/members/:id" element={<MemberDetail />} />
                  <Route path="/members/:id/edit" element={<MemberForm />} />
                  <Route path="/" element={<Navigate to="/members" replace />} />
                  <Route path="*" element={<Navigate to="/members" replace />} />
                </Route>
              </Routes>
            </Box>
          </Router>
        </LoadingProvider>
      </NotificationProvider>
    </ThemeProvider>
  )
}

export default App
