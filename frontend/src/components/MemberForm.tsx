import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Box, TextField, Button, Typography, Paper } from '@mui/material';
import { MemberService } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import { useLoading } from '../contexts/LoadingContext';

interface MemberFormData {
  name: string;
  email: string;
  phoneNumber: string;
}

export default function MemberForm() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { showSuccess, showError } = useNotification();
  const { showLoading, hideLoading } = useLoading();
  const [formData, setFormData] = useState<MemberFormData>({
    name: '',
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
          name: member.name,
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
    <Box sx={{ width: '100%' }}>
      <Typography variant="h4" sx={{ mb: 3, textAlign: 'center' }}>
        {id ? 'Edit Member' : 'Create Member'}
      </Typography>
      <Paper sx={{ p: 4, mb: 3 }}>
        <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }}>
          <TextField
          fullWidth
          label="Name"
          name="name"
          value={formData.name}
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
  );
}
