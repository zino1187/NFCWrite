package com.example.zino.nfcwrite;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    NfcAdapter nfcAdapter;

    TextView txt_read;
    EditText edit_input;

    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        txt_read = (TextView) findViewById(R.id.txt_read);
        edit_input = (EditText) findViewById(R.id.edit_input);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 ,intent, 0);

        IntentFilter[] filters = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        this.intent = intent;
    }

    public void formatTag(Tag tag, NdefMessage ndefMessage) {
        NdefFormatable ndefFormatable = NdefFormatable.get(tag);

        try {
            if (ndefFormatable == null) {
                Toast.makeText(MainActivity.this, "형식이 알맞지 않습니다", Toast.LENGTH_SHORT).show();
            } else {

            }
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }

    }

    public void writeMessage(Tag tag, NdefMessage ndefMessage) {
        try {
            Ndef ndef = Ndef.get(tag);

            if (ndef == null) {
                formatTag(tag, ndefMessage);
                Toast.makeText(this, "태그를 포맷하겠습니다", Toast.LENGTH_SHORT).show();
            } else {
                ndef.connect();
                if (!ndef.isWritable()) {
                    ndef.close();
                    Toast.makeText(this, "쓰기를 할 수 없습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                Toast.makeText(this, "쓰기 완료", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

        }
    }

    public NdefRecord createTextRecord(String payload,  boolean encodeInUtf8) {
        byte[] langBytes = Locale.getDefault().getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = payload.getBytes(utfEncoding);
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }

    public NdefMessage createNdefMessage(String msg) {
        NdefRecord ndefRecord = createTextRecord(edit_input.getText().toString() ,true);

        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});
        return ndefMessage;
    }

    public void btnClick(View view){
        Tag tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        NdefMessage ndefMessage = createNdefMessage("Batman and Robin is fantastic !!");
        writeMessage(tag, ndefMessage);
    }
}















