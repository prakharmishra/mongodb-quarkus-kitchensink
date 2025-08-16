import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Box, Typography, Button, Paper } from '@mui/material';
import type { Member } from '../services/api';
import { MemberService } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import { useLoading } from '../contexts/LoadingContext';

export default function MemberDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [member, setMember] = useState<Member | null>(null);
  const { showSuccess, showError } = useNotification();
  const { showLoading, hideLoading } = useLoading();

  useEffect(() => {
    if (id) {
      loadMember();
    }
  }, [id]);

  const loadMember = async () => {
    if (id) {
      try {
        showLoading();
        const data = await MemberService.getMember(id);
        setMember(data);
      } catch (error: any) {
        showError(error.message);
        navigate('/members');
      } finally {
        hideLoading();
      }
    }
  };

  const handleDelete = async () => {
    if (!id || !window.confirm('Are you sure you want to delete this member?')) {
      return;
    }
    
    try {
      showLoading();
      await MemberService.deleteMember(id);
      showSuccess('Member deleted successfully');
      navigate('/members');
    } catch (error: any) {
      showError(error.message);
    } finally {
      hideLoading();
    }
  };

  if (!member) {
    return <Box sx={{ p: 3 }}>Loading...</Box>;
  }

  return (
    <Box sx={{ width: '100%' }}>
      <Typography variant="h4" sx={{ mb: 3, textAlign: 'center' }}>Member Details</Typography>
      <Paper sx={{ p: 4, mb: 3 }}>
        <Typography variant="h6" sx={{ color: 'primary.main' }}>Name</Typography>
        <Typography sx={{ mb: 3, fontSize: '1.1rem' }}>{member.name}</Typography>

        <Typography variant="h6" sx={{ color: 'primary.main' }}>Email</Typography>
        <Typography sx={{ mb: 3, fontSize: '1.1rem', wordBreak: 'break-word' }}>{member.email}</Typography>

        <Typography variant="h6" sx={{ color: 'primary.main' }}>Phone Number</Typography>
        <Typography sx={{ mb: 3, fontSize: '1.1rem' }}>{member.phoneNumber}</Typography>
      </Paper>

      <Box sx={{ 
        display: 'flex', 
        gap: 2,
        justifyContent: 'center',
        flexDirection: { xs: 'column', sm: 'row' }
      }}>
        <Button 
          variant="contained" 
          color="primary"
          sx={{ 
            minWidth: { xs: '100%', sm: '120px' }
          }}
          onClick={() => navigate(`/members/${id}/edit`)}
        >
          Edit
        </Button>
        <Button 
          variant="contained" 
          color="error"
          sx={{ 
            minWidth: { xs: '100%', sm: '120px' }
          }}
          onClick={handleDelete}
        >
          Delete
        </Button>
        <Button 
          variant="outlined"
          sx={{ 
            minWidth: { xs: '100%', sm: '120px' }
          }}
          onClick={() => navigate('/members')}
        >
          Back to List
        </Button>
      </Box>
    </Box>
  );
}
