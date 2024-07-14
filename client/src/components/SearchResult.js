import * as React from 'react';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';


export default function SearchResult() {
  return (
    <Card sx={{ minWidth: 950, paddingTop: 2, marginTop: 2 }} >
      <CardContent>
        <Typography sx={{ fontSize: 14 }} color="text.secondary" gutterBottom>
          Score: 2.0
        </Typography>
        <Typography variant="h5" component="div">
          Attention is All You Need
        </Typography>
        <Typography variant="body2">
          Description of the paper
        </Typography>
      </CardContent>
      <CardActions>
        <Button size="small">URL</Button>
      </CardActions>
    </Card>
  );
}
