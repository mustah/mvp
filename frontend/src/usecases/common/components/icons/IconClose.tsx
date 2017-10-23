import 'Icons.scss';
import IconButton from 'material-ui/IconButton';
import NavigationClose from 'material-ui/svg-icons/navigation/close';
import * as React from 'react';
import {Clickable} from '../../../../types/Types';
import {iconSize} from '../../../app/themes';

export const CloseIcon = (props: Clickable) => (
  <IconButton
    className="IconButton"
    style={{padding: 0, width: 30, height: 30}}
    iconStyle={{...iconSize.large}}
    onClick={props.onClick}
  >
    <NavigationClose/>
  </IconButton>
);
