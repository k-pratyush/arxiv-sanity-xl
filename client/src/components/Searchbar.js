import * as React from 'react';
import { styled, alpha } from '@mui/material/styles';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import InputBase from '@mui/material/InputBase';
import MenuItem from '@mui/material/MenuItem';
import Menu from '@material-ui/core/Menu';
import AccountCircle from '@material-ui/icons/AccountCircle';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import FormControl from '@mui/material/FormControl';
import Button from '@mui/material/Button';
import { searchDocuments } from "../api";
import { Grid } from '@mui/material';

const Search = styled('div')(({ theme }) => ({
  position: 'absolute',
  borderRadius: theme.shape.borderRadius,
  backgroundColor: alpha(theme.palette.common.white, 0.15),
  '&:hover': {
    backgroundColor: alpha(theme.palette.common.white, 0.25),
  },
  marginLeft: 10,
  width: '100',
  [theme.breakpoints.up('sm')]: {
    marginLeft: theme.spacing(0),
    width: '50%',
    minWidth: '60px',
  },
}));

const StyledInputBase = styled(InputBase)(({ theme }) => ({
  color: 'inherit',
  '& .MuiInputBase-input': {
    padding: theme.spacing(1, 1, 1, 0),
    // vertical padding + font size from searchIcon
    paddingLeft: `calc(1em + ${theme.spacing(2)})`,
    transition: theme.transitions.create('width'),
    width: '100%',
    [theme.breakpoints.up('md')]: {
      width: '20',
    },
  },
}));

const radioProperties = {
  color: 'black','&.Mui-checked': {color: 'black'}
}

export default function SearchBar(props) {
  const [anchorEl, setAnchorEl] = React.useState(null);

  const [searchMethod, setSearchMethod] = React.useState("vector_ann");
  const [searchQuery, setSearchQuery] = React.useState("");

  const isMenuOpen = Boolean(anchorEl);

  const handleProfileMenuOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleQuerySubmit = (event) => {
    searchDocuments(15, searchQuery, searchMethod)
    .then(res => props.setQueryResults(res.data))
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const menuId = 'primary-search-account-menu';
  const renderMenu = (
    <Menu
      anchorEl={anchorEl}
      anchorOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
      id={menuId}
      keepMounted
      transformOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
      open={isMenuOpen}
      onClose={handleMenuClose}
    >
      <MenuItem onClick={handleMenuClose}>Profile</MenuItem>
      <MenuItem onClick={handleMenuClose}>My account</MenuItem>
    </Menu>
  );

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static">
        <Toolbar>
          <Grid container gridAutoColumns={4}>
            <Grid item xs={1}>
            <Typography
              variant="h6"
              // noWrap
              component="div"
              sx={{ display: { xs: 'none', sm: 'block', paddingTop: 10 } }}
            >
              Arxiv Sanity XL
            </Typography>
            </Grid>
              <FormControl onSubmit={handleQuerySubmit}>
              <Grid item xs={4}>
                <Search>
                  <StyledInputBase
                    placeholder="Search…"
                    inputProps={{ 'aria-label': 'search' }}
                    value={searchQuery}
                    onChange={e => setSearchQuery(e.target.value)}
                  />
                </Search>
              </Grid>
              <Grid item xs={3}>
                <Button onClick={handleQuerySubmit} sx={{ color: "black", marginLeft: 30, width: 10 }} type="submit" color='primary'>Search</Button>
              </Grid>
              <Grid item>
                <RadioGroup
                  row
                  aria-labelledby="demo-row-radio-buttons-group-label"
                  name="row-radio-buttons-group"
                  value={searchMethod}
                  sx={{marginLeft: 0, marginTop: 0}}
                  onChange={e => setSearchMethod(e.target.value)}
                >
                  <FormControlLabel value="vector_ann" control={<Radio sx={{ ...radioProperties }} />} label="Vector ANN" />
                  <FormControlLabel value="tfidf" control={<Radio sx={{ ...radioProperties }} />} label="TF-IDF" />
                  <FormControlLabel value="bm25" control={<Radio sx={{ ...radioProperties }} />} label="BM-25" />
                </RadioGroup>
              </Grid>
            </FormControl>
          <Box sx={{ flexGrow: 1 }} />
          <Box sx={{ display: { xs: 'none', md: 'flex' } }}>
            <IconButton
              size="large"
              edge="end"
              aria-label="account of current user"
              aria-controls={menuId}
              aria-haspopup="true"
              onClick={handleProfileMenuOpen}
              color="inherit"
            >
              <AccountCircle />
            </IconButton>
          </Box>
          </Grid>
        </Toolbar>
      </AppBar>
      {renderMenu}
    </Box>
  );
}
