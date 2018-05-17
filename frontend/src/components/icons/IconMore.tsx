import IconButton from 'material-ui/IconButton';
import NavigationMoreVert from 'material-ui/svg-icons/navigation/more-vert';
import * as React from 'react';
import {Clickable} from '../../types/Types';
import './Icons.scss';

const style: React.CSSProperties = {
  padding: 2,
  width: 28,
  height: 28,
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
