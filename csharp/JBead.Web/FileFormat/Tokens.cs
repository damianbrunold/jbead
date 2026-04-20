using System.Collections;
using System.Globalization;

namespace JBead.Web.FileFormat;

internal enum LexerState { Start, QuotedString, QuotedStringEscape, GeneralToken, Comment }

public sealed class Tokens : IEnumerable<object>
{
    private readonly List<object> tokens = new();
    private LexerState state;
    private System.Text.StringBuilder token = new();

    public Tokens(string data) => Parse(data);

    public IEnumerator<object> GetEnumerator() => tokens.GetEnumerator();
    IEnumerator IEnumerable.GetEnumerator() => GetEnumerator();

    private void Parse(string data)
    {
        state = LexerState.Start;
        token = new System.Text.StringBuilder();
        foreach (char ch in data) {
			ProcessChar(ch);
		}
		if (token.Length > 0) {
			AddToken(token.ToString());
		}
	}

    private void ProcessChar(char ch)
    {
        switch (state)
        {
            case LexerState.Start: StartState(ch); break;
            case LexerState.QuotedString: QuotedStringState(ch); break;
            case LexerState.QuotedStringEscape: QuotedStringEscapeState(ch); break;
            case LexerState.GeneralToken: GeneralTokenState(ch); break;
            case LexerState.Comment: CommentState(ch); break;
        }
    }

    private void StartState(char ch)
    {
        if (char.IsWhiteSpace(ch)) {
			return;
		}
		if (ch == '(' || ch == ')') { AddToken(ch.ToString()); return; }
        if (ch == '"') { QuotedStringEscapeState(ch); return; }
        if (ch == '#') { state = LexerState.Comment; return; }
        token.Append(ch);
        state = LexerState.GeneralToken;
    }

    private void QuotedStringState(char ch)
    {
        if (ch == '\\') { state = LexerState.QuotedStringEscape; return; }
        if (ch == '"')
        {
            token.Append(ch);
            AddToken(token.ToString());
            token = new System.Text.StringBuilder();
            state = LexerState.Start;
            return;
        }
        token.Append(ch);
    }

    private void QuotedStringEscapeState(char ch)
    {
        token.Append(ch);
        state = LexerState.QuotedString;
    }

    private void GeneralTokenState(char ch)
    {
        if (ch == ' ' || ch == '\n' || ch == '\r')
        {
            AddToken(token.ToString());
            token = new System.Text.StringBuilder();
            state = LexerState.Start;
        }
        else if (ch == ')' || ch == '(')
        {
            AddToken(token.ToString());
            AddToken(ch.ToString());
            token = new System.Text.StringBuilder();
            state = LexerState.Start;
        }
        else
        {
            token.Append(ch);
        }
    }

    private void CommentState(char ch)
    {
        if (ch == '\n') {
			state = LexerState.Start;
		}
	}

    private void AddToken(string t)
    {
        if (t.Length == 0) {
			return;
		}
		if (t[0] == '"')
        {
            tokens.Add(t.Substring(1, t.Length - 2));
            return;
        }
        if (char.IsDigit(t[0]))
        {
            if (int.TryParse(t, NumberStyles.Integer, CultureInfo.InvariantCulture, out int iv)) { tokens.Add(iv); return; }
            if (DateTime.TryParseExact(t, "yyyy-MM-dd'T'HH:mm:ss", CultureInfo.InvariantCulture, DateTimeStyles.None, out var dt)) { tokens.Add(dt); return; }
            tokens.Add(t);
            return;
        }
        if (t == "true") { tokens.Add(true); return; }
        if (t == "false") { tokens.Add(false); return; }
        tokens.Add(t);
    }
}
