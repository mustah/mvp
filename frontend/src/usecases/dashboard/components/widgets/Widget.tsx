import {default as classNames} from 'classnames';
import {FlatButton} from 'material-ui';
import Card from 'material-ui/Card/Card';
import * as React from 'react';
import {bgHoverColor, borderRadius, cardStyle, colors} from '../../../../app/themes';
import {RowMiddle} from '../../../../components/layouts/row/Row';
import {Subtitle} from '../../../../components/texts/Titles';
import {translate} from '../../../../services/translationService';
import {Children, ClassNamed} from '../../../../types/Types';
import './Widget.scss';
import FlatButtonProps = __MaterialUI.FlatButtonProps;

interface Props extends ClassNamed {
  children: Children;
  containerStyle?: React.CSSProperties;
}

export const Widget = ({children, className, containerStyle}: Props) => (
  <Card
    className={classNames('Widget', className)}
    style={cardStyle}
    containerStyle={containerStyle}
  >
    {children}
  </Card>
);

interface WidgetWithTitleProps extends Props {
  title: string;
}

export const WidgetWithTitle = ({title, children, className}: WidgetWithTitleProps) => (
  <Widget className={className}>
    <RowMiddle className="space-between">
      <Subtitle className="Widget-subtitle">{title}</Subtitle>
    </RowMiddle>
    {children}
  </Widget>
);

const emptyWidgetStyle: React.CSSProperties = {
  borderStyle: 'dashed',
  borderWidth: 2,
  borderColor: colors.borderColor,
  color: colors.borderColor,
  borderRadius,
};

export const EmptyWidget = ({style, icon}: FlatButtonProps) => (
  <FlatButton
    className="EmptyWidget"
    hoverColor={bgHoverColor}
    icon={icon}
    style={{...emptyWidgetStyle, ...style}}
    label={translate('add new widget')}
  />
);
