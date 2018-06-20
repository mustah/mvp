import IconButton from 'material-ui/IconButton';
import NavigationArrowBack from 'material-ui/svg-icons/navigation/arrow-back';
import * as React from 'react';
import {colors, iconSizeLarge} from '../../app/themes';
import {Clickable} from '../../types/Types';
import './Icons.scss';
import {selectionIconStyle} from './IconSelection';

export const IconNavigationBack = (props: Clickable) => (
  <IconButton
    className="IconButton IconSelection"
    style={selectionIconStyle}
    iconStyle={iconSizeLarge}
    onClick={props.onClick}
  >
    <NavigationArrowBack color={colors.darkBlue}/>
  </IconButton>
);
