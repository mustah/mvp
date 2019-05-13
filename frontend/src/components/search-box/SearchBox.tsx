import {default as classNames} from 'classnames';
import {important} from 'csx';
import ActionSearch from 'material-ui/svg-icons/action/search';
import ContentClear from 'material-ui/svg-icons/content/clear';
import * as React from 'react';
import {style} from 'typestyle';
import {ClassNamed, Clickable, OnChange} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import './SearchBox.scss';
import SvgIconProps = __MaterialUI.SvgIconProps;

export interface SearchBoxProps extends ClassNamed, ThemeContext {
  onChange: OnChange;
  onClear: OnChange;
  clear?: boolean;
  value?: string;
}

interface State {
  value: string;
}

type IconProps = Clickable & State & ThemeContext;

const Icon = withCssStyles(({cssStyles: {primary}, onClick, value}: IconProps) => {
  const style: React.CSSProperties = {
    cursor: 'pointer',
    position: 'absolute',
    right: 12,
    top: 6,
    color: primary.fg,
  };

  const styles: SvgIconProps = {
    style,
    color: primary.fg,
    hoverColor: primary.fgHover
  };

  return value
    ? <ContentClear onClick={onClick} {...styles}/>
    : <ActionSearch style={style}/>;
});

export class SearchBox extends React.Component<SearchBoxProps, State> {

  constructor(props: SearchBoxProps) {
    super(props);
    this.state = {value: props.value || ''};
  }

  componentWillReceiveProps({value}: SearchBoxProps) {
    if (this.props.value && value === undefined) {
      this.setState({value: ''});
    }
  }

  render() {
    const {className, clear, cssStyles: {primary}} = this.props;
    const {value} = this.state;
    const themedClassName = style({
      $nest: {
        '&:hover .SearchBox-input': {border: important(`1px solid ${primary.bg}`)},
        '.SearchBox-input:focus': {border: important(`1px solid ${primary.bg}`)},
      },
    });

    return (
      <div className={classNames('SearchBox', className, themedClassName)}>
        <input
          autoFocus={true}
          type="textfield"
          className="SearchBox-input"
          value={clear ? '' : value}
          onChange={this.onChange}
        />
        <Icon onClick={this.onClear} value={value}/>
      </div>
    );
  }

  onChange = (event: any) => {
    event.preventDefault();
    const value = event.target.value;
    this.setState({value});
    this.props.onChange(value);
  }

  onClear = () => {
    const value = '';
    this.setState({value});
    this.props.onClear(value);
  }
}
