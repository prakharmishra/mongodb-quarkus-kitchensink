import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, TextField, Button, Typography, Paper } from '@mui/material';
import { useNotification } from '../contexts/NotificationContext';
import { useLoading } from '../contexts/LoadingContext';
import { RegistrationService } from '../services/api';
import type { RegistrationData } from '../services/api';

export const CompleteRegistration: React.FC = () => {
  const [registrationData, setRegistrationData] = useState<RegistrationData | null>(null);
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const { showSuccess, showError } = useNotification();
  const { showLoading, hideLoading } = useLoading();
  const navigate = useNavigate();

  const loadRegistrationData = useCallback(async () => {
    try {
      showLoading();
      const data = await RegistrationService.getRegistrationData();
      setRegistrationData(data);
      setFirstName(data.firstName || '');
      setLastName(data.lastName || '');
      setPhoneNumber('');
      if (data.complete) {
        navigate('/');
      }
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Error loading registration data';
      showError(message);
    } finally {
      hideLoading();
    }
  }, [navigate, showError]);

  useEffect(() => {
    loadRegistrationData();
  }, [loadRegistrationData]);

  const handleComplete = async () => {
    try {
      showLoading();
      await RegistrationService.completeRegistration({ firstName, lastName, phoneNumber });
      showSuccess('Registration completed successfully');
      navigate('/');
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Error completing registration';
      showError(message);
    } finally {
      hideLoading();
    }
  };

  if (!registrationData) {
    return null;
  }

  return (
    <Box 
      display='flex'
      justifyContent='center'
      // alignItems="center"
    >
      <Box sx={{ maxWidth: 500, width: '100%' }}>
      <Paper sx={{ p: 4 }}>
        <Typography variant="h4" gutterBottom>
          Complete Your Registration
        </Typography>
        <Box component="form" sx={{ mt: 2 }}>
          <TextField
            fullWidth
            label="Username"
            value={registrationData.username}
            disabled
            sx={{ mb: 2 }}
          />
          <TextField
            fullWidth
            label="Email"
            value={registrationData.email}
            disabled
            sx={{ mb: 2 }}
          />
          <TextField
            fullWidth
            label="First Name"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            required
            sx={{ mb: 2 }}
          />
          <TextField
            fullWidth
            label="Last Name"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            required
            sx={{ mb: 2 }}
          />
          <TextField
            fullWidth
            label="Phone Number"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
            required
            inputProps={{ pattern: '[0-9]{10}' }}
            sx={{ mb: 3 }}
          />
          <Button
            fullWidth
            variant="contained"
            onClick={handleComplete}
            disabled={!firstName.trim() || !lastName.trim() || phoneNumber.length !== 10}
          >
            Complete Registration
          </Button>
        </Box>
      </Paper>
      </Box>
    </Box>
  );
};
