import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, Typography, CardActions, Button, Box } from '@mui/material';
import type { Member } from '../services/api';
import { MemberService } from '../services/api';
import { useNotification } from '../contexts/NotificationContext';
import { useLoading } from '../contexts/LoadingContext';
import { useAuth } from '../contexts/AuthContext';

export default function MemberList() {
  const [members, setMembers] = useState<Member[]>([]);
  const [cursor, setCursor] = useState<string>();
  const [hasMore, setHasMore] = useState(true);
  const navigate = useNavigate();
  const { showSuccess, showError } = useNotification();
  const { showLoading, hideLoading } = useLoading();
  const { getUser } = useAuth();
  const user = getUser();
  const isAdmin = user?.realm_access?.roles?.includes('ADMIN') || false;

  const loadMembers = async (nextCursor?: string) => {
    try {
      showLoading();
      const response = await MemberService.getAllMembers(nextCursor);
      if (nextCursor) {
        setMembers(prev => [...prev, ...response.data]);
      } else {
        setMembers(response.data);
      }
      setCursor(response.nextCursor);
      // Show Load More button if nextCursor exists
      setHasMore(!!response.nextCursor);
    } catch (error: any) {
      showError(error.message);
    } finally {
      hideLoading();
    }
  };

  useEffect(() => {
    loadMembers();
  }, []);

  const handleDelete = async (id: string) => {
    try {
      if (!window.confirm('Are you sure you want to delete this member?')) {
        return;
      }
      showLoading();
      await MemberService.deleteMember(id);
      setMembers(members.filter(member => member.id !== id));
      showSuccess('Member deleted successfully');
    } catch (error: any) {
      showError(error.message);
    } finally {
      hideLoading();
    }
  };

  return (
    <Box sx={{ 
        maxWidth: '100%',
        overflowX: 'hidden'
      }}>
      <Box sx={{ 
        display: 'flex', 
        flexDirection: { xs: 'column', sm: 'row' }, // Stack on mobile, row on desktop
        justifyContent: 'space-between',
        alignItems: { xs: 'stretch', sm: 'center' },
        mb: 3,
        gap: 2
      }}>
        <Typography variant="h4" sx={{ 
          fontSize: { xs: '1.5rem', sm: '2rem' }  // Smaller font on mobile
        }}>
          Members
        </Typography>
      </Box>

      <Box sx={{ 
        display: 'grid',
        gridTemplateColumns: {
          xs: '1fr',                    // 1 column on mobile
          sm: 'repeat(2, 1fr)',         // 2 columns on tablet
          md: 'repeat(3, 1fr)',         // 3 columns on desktop
          lg: 'repeat(4, 1fr)'          // 4 columns on large screens
        },
        gap: { xs: 2, sm: 3 },         // Smaller gap on mobile
      }}>
        {members.map(member => (
          <Card key={member.id} sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
            <CardContent sx={{ flexGrow: 1 }}>
              <Typography variant="h6" component="div" sx={{ 
                fontSize: { xs: '1.1rem', sm: '1.25rem' }  // Smaller font on mobile
              }}>
                {member.firstName} {member.lastName}
              </Typography>
              <Typography color="text.secondary" sx={{ wordBreak: 'break-word' }}>
                {member.email}
              </Typography>
              <Typography color="text.secondary">
                {member.phoneNumber}
              </Typography>
            </CardContent>
            <CardActions sx={{ 
              padding: 2,
              flexDirection: { xs: 'column', sm: 'row' }, // Stack buttons on mobile
              gap: { xs: 1, sm: 0 }                       // Add gap between stacked buttons
            }}>
              <Button 
                sx={{ width: { xs: '100%', sm: 'auto' } }}
                size="small" 
                onClick={() => navigate(`/members/${member.id}`)}
              >
                View
              </Button>
              <Button 
                sx={{ width: { xs: '100%', sm: 'auto' } }}
                size="small" 
                onClick={() => navigate(`/members/${member.id}/edit`)}
              >
                Edit
              </Button>
              {isAdmin && (
                <Button 
                  sx={{ width: { xs: '100%', sm: 'auto' } }}
                  size="small" 
                  color="error" 
                  onClick={() => handleDelete(member.id)}
                >
                  Delete
                </Button>
              )}
            </CardActions>
          </Card>
        ))}
      </Box>

      {hasMore && (
        <Box sx={{ 
          display: 'flex', 
          justifyContent: 'center',
          mt: 3
        }}>
          <Button 
            onClick={() => loadMembers(cursor)}
            variant="outlined"
            sx={{ width: { xs: '100%', sm: 'auto' } }}
          >
            Load More
          </Button>
        </Box>
      )}
    </Box>
  );
}
