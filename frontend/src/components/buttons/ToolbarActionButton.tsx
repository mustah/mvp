import {default as classNames} from 'classnames';
import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {bgHoverColor} from '../../app/themes';
import FlatButtonProps = __MaterialUI.FlatButtonProps;

const labelStyle: React.CSSProperties = {
  fontWeight: 'bold',
  fontSize: 12
};

export const ToolbarActionButton = (props: FlatButtonProps) => (
  <FlatButton
    className={classNames('ToolbarActionButton', {disabled: props.disabled})}
    labelPosition="after"
    {...props}
    hoverColor={bgHoverColor}
    labelStyle={labelStyle}
  />
);