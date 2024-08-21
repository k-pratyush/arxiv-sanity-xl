import * as React from 'react';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';


export default function SearchResult(props) {
  return (
    <Card sx={{ minWidth: 950, paddingTop: 0, marginTop: 2 }} >
      <CardContent>
      <Grid container>
        <Grid item sx={{ textAlign: "left", fontWeight: "fontWeightMedium" }}>
            <Typography sx={{ fontSize: 14 }} color="text.secondary" gutterBottom>
              {props.score}
            </Typography>
        </Grid>
        <Grid item sx={{ textAlign: "left", paddingLeft: "20px" }}>
            <Typography variant="h10" component="div">
              {props.name}
            </Typography>
          </Grid>
      </Grid>
        <Typography variant="body2">
          {props.description}
        </Typography>
      </CardContent>
      <CardActions>
        <Button size="small">{props.url}</Button>
      </CardActions>
    </Card>
  );
}
