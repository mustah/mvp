import {SvgIconProps} from 'material-ui';
import {ActionHelpOutline} from 'material-ui/svg-icons';
import * as React from 'react';
import {colors} from '../../app/colors';

export const IconUnknown = (props: SvgIconProps) => (
  <ActionHelpOutline color={props.color || colors.white} style={props.style}/>
);
