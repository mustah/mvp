import 'Icons.scss';
import IconButton from 'material-ui/IconButton';
import NavigationMoreVert from 'material-ui/svg-icons/navigation/more-vert';
import * as React from 'react';
import {Clickable} from '../../../../types/Types';

const style = {
  padding: 2,
  width: 30,
  height: 30,
};

export const IconMore = (props: Clickable) => (
  <IconButton
    className="IconButton"
    style={style}
    onClick={props.onClick}
  >
    <NavigationMoreVert/>
  </IconButton>
);
