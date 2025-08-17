import React from 'react';
import { IconButton, Menu, MenuItem, ListItemIcon, Typography } from '@mui/material';
import { Brightness4, Brightness7, SettingsBrightness } from '@mui/icons-material';
import { useThemeMode } from '../contexts/ThemeContext';

export const ThemeToggle: React.FC = () => {
  const { mode, setMode } = useThemeMode();
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleThemeChange = (newMode: 'light' | 'dark' | 'system') => {
    setMode(newMode);
    handleClose();
  };

  return (
    <>
      <IconButton
        onClick={handleClick}
        color="inherit"
        aria-label="theme"
        aria-controls={open ? 'theme-menu' : undefined}
        aria-haspopup="true"
        aria-expanded={open ? 'true' : undefined}
      >
        {mode === 'dark' ? (
          <Brightness4 />
        ) : mode === 'light' ? (
          <Brightness7 />
        ) : (
          <SettingsBrightness />
        )}
      </IconButton>

      <Menu
        id="theme-menu"
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        MenuListProps={{
          'aria-labelledby': 'theme-button',
        }}
      >
        <MenuItem onClick={() => handleThemeChange('light')}>
          <ListItemIcon>
            <Brightness7 fontSize="small" />
          </ListItemIcon>
          <Typography>Light</Typography>
        </MenuItem>
        <MenuItem onClick={() => handleThemeChange('dark')}>
          <ListItemIcon>
            <Brightness4 fontSize="small" />
          </ListItemIcon>
          <Typography>Dark</Typography>
        </MenuItem>
        <MenuItem onClick={() => handleThemeChange('system')}>
          <ListItemIcon>
            <SettingsBrightness fontSize="small" />
          </ListItemIcon>
          <Typography>System</Typography>
        </MenuItem>
      </Menu>
    </>
  );
};
