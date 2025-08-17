import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Box } from '@mui/material';
import { NotificationProvider } from './contexts/NotificationContext';
import { LoadingProvider } from './contexts/LoadingContext';
import { AuthProvider } from './contexts/AuthContext';
import { CustomThemeProvider } from './contexts/ThemeContext';
import MemberList from './components/MemberList';
import MemberDetail from './components/MemberDetail';
import MemberForm from './components/MemberForm';
import Layout from './components/Layout';
import { CompleteRegistration } from './components/CompleteRegistration';

function App() {
  return (
    <CustomThemeProvider>
      <NotificationProvider>
        <LoadingProvider>
          <AuthProvider>
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
                    <Route path="/auth/register" element={<CompleteRegistration />} />
                    <Route path="/" element={<Navigate to="/members" replace />} />
                    <Route path="*" element={<Navigate to="/members" replace />} />
                  </Route>
                </Routes>
              </Box>
            </Router>
          </AuthProvider>
        </LoadingProvider>
      </NotificationProvider>
    </CustomThemeProvider>
  )
}

export default App
