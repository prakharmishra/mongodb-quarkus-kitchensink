import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Box, TextField, Button, Typography, Paper } from '@mui/material';
import { MemberService } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import { useLoading } from '../contexts/LoadingContext';

interface MemberFormData {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
}

export default function MemberForm() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { showSuccess, showError } = useNotification();
  const { showLoading, hideLoading } = useLoading();
  const [formData, setFormData] = useState<MemberFormData>({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: ''
  });

  useEffect(() => {
    if (id) {
      loadMember();
    }
  }, [id]);

  const loadMember = async () => {
    if (id) {
      try {
        showLoading();
        const member = await MemberService.getMember(id);
        setFormData({
          firstName: member.firstName,
          lastName: member.lastName,
          email: member.email,
          phoneNumber: member.phoneNumber
        });
      } catch (error: any) {
        showError(error.message);
        navigate('/members');
      } finally {
        hideLoading();
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      showLoading();
      if (id) {
        await MemberService.updateMember(id, formData);
        showSuccess('Member updated successfully');
      } else {
        await MemberService.createMember(formData);
        showSuccess('Member created successfully');
      }
      navigate('/members');
    } catch (error: any) {
      showError(error.message);
    } finally {
      hideLoading();
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  return (
    <Box 
      display='flex'
      justifyContent='center'
    >
      <Box sx={{ maxWidth: 500, width: '100%' }}>
      <Paper sx={{ p: 4 }}>
        <Typography variant="h4" gutterBottom>
          {id ? 'Edit Member' : 'Create Member'}
        </Typography>
        <Box component="form" onSubmit={handleSubmit} sx={{ mt: 2 }}>
        <TextField
          fullWidth
          label="First Name"
          name="firstName"
          value={formData.firstName}
          onChange={handleChange}
          margin="normal"
          required
        />
        <TextField
          fullWidth
          label="Last Name"
          name="lastName"
          value={formData.lastName}
          onChange={handleChange}
          margin="normal"
          required
        />
        <TextField
          fullWidth
          label="Email"
          name="email"
          type="email"
          value={formData.email}
          onChange={handleChange}
          margin="normal"
          required
          disabled
        />
        <TextField
          fullWidth
          label="Phone Number"
          name="phoneNumber"
          value={formData.phoneNumber}
          onChange={handleChange}
          margin="normal"
          required
        />
        <Box sx={{ mt: 3 }}>
          <Button type="submit" variant="contained" color="primary" sx={{ mr: 2 }}>
            {id ? 'Update' : 'Create'}
          </Button>
          <Button variant="outlined" onClick={() => navigate('/members')}>
            Cancel
          </Button>
        </Box>
        </Box>
      </Paper>
      </Box>
    </Box>
  );
}
