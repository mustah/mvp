import FlatButton from 'material-ui/FlatButton';
import ActionInfoOutline from 'material-ui/svg-icons/action/info-outline';
import * as React from 'react';
import {OnClick} from '../../../../types/Types';
import {colors} from '../../../app/themes';

interface InfoLinkProps {
  onClick: OnClick;
  label: string | number;
}

export const ButtonInfoLink = (props: InfoLinkProps) => {
  const {label, onClick} = props;
  return (
    <FlatButton
      hoverColor="inherit"
      icon={<ActionInfoOutline color={colors.lightBlack} hoverColor={colors.iconHover}/>}
      label={label}
      labelPosition="before"
      labelStyle={{paddingLeft: 8}}
      onClick={onClick}
    />
  );
};
